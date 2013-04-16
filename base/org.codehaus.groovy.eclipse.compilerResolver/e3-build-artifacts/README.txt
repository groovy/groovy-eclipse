The files in this folder are for non-e4 builds.  In e4, the model starts up
early and causes the PackageExplorer part to be started. This in turn causes JDT to be started
very early.  By using the ModelProcessor, we can force this bundle to be loaded before the model 
starts.

So, in e38 and earlier, there are no e4 compnents.  They should be absent from the distribution.