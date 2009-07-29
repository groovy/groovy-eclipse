/*
 This DLL provides some native utility methods for the Performance Plugin.

 2003/07/08 - I tried providing my own replacement to GetPerformanceInfo by calling pdhopenquery()
 but wsad always crashed when I shut it down, so I backed off (at least for now) and simply return
 a function not available return code. At some later point in time you can go back in VSS and restore
 that code and try again.
 */

#include <windows.h>
#include <wincon.h>
#include <psapi.h>

#include "ivjperf.h"

enum loadStatusType {unknown, loaded, notFound} loadStatus = unknown, loadHandleCount = unknown;

// loadStatusType loadStatus;

// depending on the OS (NT4/W2k or XP) we use different functions
typedef BOOL WINAPI _GPI(PPERFORMACE_INFORMATION pPerformanceInformation, DWORD cb);

_GPI* gpGetPerformanceInfo = NULL;	// this is our function pointer

typedef BOOL WINAPI _GPHC(HANDLE hProcess, PDWORD count);
_GPHC* gpGetProcessHandleCount = NULL;


/*
	A helper function that makes it easier to throw exceptions.
*/
void throwException(JNIEnv * jniEnv, char* details)
{
	jclass exceptionClass;

	exceptionClass = (*jniEnv)->FindClass(jniEnv, "java/lang/IllegalArgumentException"); 
	if (exceptionClass == 0)
	{
		printf("Could not find the exception class I have to give up");
		return;
	}
	(*jniEnv)->ThrowNew(jniEnv, exceptionClass, details);
	return;

}

/*
	A helper function that throws an exception that tells us a function is not supported
*/
void throwUnsupported(JNIEnv * jniEnv, char* details)
{
	jclass exceptionClass;

	exceptionClass = (*jniEnv)->FindClass(jniEnv, "java/lang/UnsupportedOperationException"); 
	if (exceptionClass == 0)
	{
		printf("Could not find the UnsupportedOperationException class I have to give up");
		return;
	}
	(*jniEnv)->ThrowNew(jniEnv, exceptionClass, details);
	return;

}

/*
	A helper method that makes it easier to handle errors. If a windows
	error is encountered, then a RuntimeException is thrown.
*/
void handleSystemError(JNIEnv * jniEnv)
{
	jclass exceptionClass;
	LPVOID lpMsgBuf;

	if (!FormatMessage( 
		FORMAT_MESSAGE_ALLOCATE_BUFFER | 
		FORMAT_MESSAGE_FROM_SYSTEM | 
		FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL,
		GetLastError(),
		MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
		(LPTSTR) &lpMsgBuf,
		0,
		NULL ))
	{
		// I give up
		return;
	}

	exceptionClass = (*jniEnv)->FindClass(jniEnv, "java/lang/RuntimeException"); 
	(*jniEnv)->ThrowNew(jniEnv, exceptionClass, lpMsgBuf);
	LocalFree( lpMsgBuf );
}

/*
	Determine if you can (or have) loaded the GetPerformanceInfo function. Set the global variable
	loadStatus with your determination.
*/
void checkGetPerformanceInfo(JNIEnv * jniEnv)
{
	if (loadStatus == unknown)
	{
		HMODULE psapiHandle = LoadLibraryA("PSAPI.dll");
		if (psapiHandle == NULL)
		{
			loadStatus = notFound;
			throwUnsupported(jniEnv, "Could not load psapi.dll");
			return;
		}
		gpGetPerformanceInfo = (_GPI*)GetProcAddress(psapiHandle, "GetPerformanceInfo");

		if(gpGetPerformanceInfo == NULL)loadStatus = notFound;
		else loadStatus = loaded;
	}
}

/*
	If you can answer the total amount of committed memory (for the entire machine). If you can't
	figure this out then return -1.
*/
jlong getTotalCommitted(JNIEnv * jniEnv)
{
	BOOL rc;
	jlong result = -1;
	PERFORMACE_INFORMATION	pi;

	checkGetPerformanceInfo(jniEnv);
	if (loadStatus == loaded)
	{
		rc = (gpGetPerformanceInfo)(&pi, sizeof(pi));
		if (!rc)handleSystemError(jniEnv);
		else result = pi.CommitTotal * pi.PageSize;
	}
	return result;
}

/*
	Answer the number of open handles in the process. If you can't get this information return -1.
*/
jlong getHandleCount(JNIEnv * jniEnv, HANDLE me)
{
	jlong result = -1;
	DWORD handleCount;

	if (loadHandleCount == unknown)
	{
		HMODULE kernel32 = LoadLibraryA("kernel32.dll");
		if (kernel32 == NULL)
		{
			loadHandleCount = notFound;
			throwUnsupported(jniEnv, "Could not load kernel32.dll");
		}
		else
		{
			gpGetProcessHandleCount = (_GPHC*)GetProcAddress(kernel32, "GetProcessHandleCount");

			if(gpGetProcessHandleCount == NULL)loadHandleCount = notFound;
			else loadHandleCount = loaded;
		}
	}

	if (loadHandleCount == loaded)
	{
		(gpGetProcessHandleCount)(me, &handleCount);
		result = handleCount;
	}
	return result;
}

/*
	The following block is copied from the Java source code. It documents the counters array.

	 * @param counters the results are returned in this array.
	 * <ol>
	 * <li>working set in bytes for this process
	 * <li>peak working set in bytes for this process
	 * <li>elapsed time in milliseconds
	 * <li>user time in milliseconds
	 * <li>kernel time in milliseconds
	 * <li>page faults for the process
	 * <li>commit charge total in bytes (working set for the entire machine). On some 
	 * machines we have problems getting this value so we return -1 in that case.
	 * <li>number of GDI objects in the process
	 * <li>number of USER objects in the process
	 * <li>number of open handles in the process. returns -1 if this information is not available
	 * <li>Number of read operations
	 * <li>Number of write operations
	 * <li>Number of bytes read
	 * <li>Number of bytes written
	 * </ol>

*/
JNIEXPORT jboolean JNICALL Java_org_eclipse_perfmsr_core_PerformanceMonitor_nativeGetPerformanceCounters
  (JNIEnv * jniEnv, jclass jniClass, jlongArray counters)
{
	FILETIME creationTime, exitTime, kernelTime, userTime, systemTime;
	ULARGE_INTEGER uliCreation, uliSystem, uliKernel, uliUser;
	ULONGLONG diff;
	IO_COUNTERS ioCounters;

	jboolean result = TRUE;
	jsize len = (*jniEnv)->GetArrayLength(jniEnv, counters);
	jlong *body = (*jniEnv)->GetLongArrayElements(jniEnv, counters, 0);
	HANDLE me = GetCurrentProcess();
	PROCESS_MEMORY_COUNTERS memCounters;
	DWORD cb = sizeof(PROCESS_MEMORY_COUNTERS);
	BOOL rc = GetProcessMemoryInfo(me, &memCounters, cb);
	if (rc != 0)
	{
		body[0] = memCounters.WorkingSetSize;
		body[1] = memCounters.PeakWorkingSetSize;
		body[5] = memCounters.PageFaultCount;
	}
	else
	{
		handleSystemError(jniEnv);
		return FALSE;
	}

	if (!GetProcessTimes(me, &creationTime, &exitTime, &kernelTime, &userTime))
	{
		handleSystemError(jniEnv);
		return FALSE;
	}
	GetSystemTimeAsFileTime(&systemTime);

	memcpy(&uliCreation, &creationTime, sizeof(uliCreation));  
	memcpy(&uliSystem, &systemTime, sizeof(uliSystem));
	memcpy(&uliKernel, &kernelTime, sizeof(uliSystem));
	memcpy(&uliUser, &userTime, sizeof(ULARGE_INTEGER));
	diff = uliSystem.QuadPart - uliCreation.QuadPart;
	body[2] = diff / 10000;
	body[3] = uliUser.QuadPart / 10000;
	body[4] = uliKernel.QuadPart / 10000;
	body[6] = getTotalCommitted(jniEnv);

	body[7] = GetGuiResources(me, GR_GDIOBJECTS);
	body[8] = GetGuiResources(me, GR_USEROBJECTS);
	body[9] = getHandleCount(jniEnv, me);

	if (!GetProcessIoCounters(me, &ioCounters))
	{
		handleSystemError(jniEnv);
		return FALSE;
	}
	body[10] = ioCounters.ReadOperationCount;
	body[11] = ioCounters.WriteOperationCount;
	body[12] = ioCounters.ReadTransferCount;
	body[13] = ioCounters.WriteTransferCount;

	(*jniEnv)->ReleaseLongArrayElements(jniEnv, counters, body, 0);

	return result;
}

JNIEXPORT jstring JNICALL Java_org_eclipse_perfmsr_core_PerformanceMonitor_nativeGetUUID
  (JNIEnv * jniEnv, jclass jniClass)
{
	UUID				uuid;
	unsigned char*		uuidStr;
	jstring				result;

	UuidCreate(&uuid);
	UuidToString(&uuid, &uuidStr);

	result = (*jniEnv)->NewStringUTF(jniEnv, uuidStr); 
	RpcStringFree(&uuidStr);

	return result;
}


JNIEXPORT void JNICALL Java_org_eclipse_perfmsr_core_PerformanceMonitor_nativeGetPerformanceInfo
  (JNIEnv * jniEnv, jclass jniClass, jlongArray counters)
{
	jlong *body;
	BOOL rc;
	PERFORMACE_INFORMATION	pi;

	jsize len = (*jniEnv)->GetArrayLength(jniEnv, counters); 
	
	if (len != 13)
	{
		throwException(jniEnv, "Wrong number of elements in array, 13 are expected");
		return;
	}

	checkGetPerformanceInfo(jniEnv);
	if (loadStatus == notFound)
	{
		throwUnsupported(jniEnv, "The GetPerformanceInfo() function is not available");
		return;
	}
	
	body = (*jniEnv)->GetLongArrayElements(jniEnv, counters, 0);
	
	rc = (gpGetPerformanceInfo)(&pi, sizeof(pi));
	if (!rc)
	{
		handleSystemError(jniEnv);
		return;
	}

	body[0] = pi.CommitTotal;
	body[1] = pi.CommitLimit;
	body[2] = pi.CommitPeak;
	body[3] = pi.PhysicalTotal;
	body[4] = pi.PhysicalAvailable;
	body[5] = pi.SystemCache;
	body[6] = pi.KernelTotal;
	body[7] = pi.KernelPaged;
	body[8] = pi.KernelNonpaged;
	body[9] = pi.PageSize;
	body[10] = pi.HandleCount;
	body[11] = pi.ProcessCount;
	body[12] = pi.ThreadCount;

	(*jniEnv)->ReleaseLongArrayElements(jniEnv, counters, body, 0);

}

