
def t = [ x:1, y:2 ] // 1

def shiftTriangle = { it -> // 12
	it.x += 1 // 2
	it.y += 1 // 3

	it.getX()
}

t.getX() // 4

println "Triangle is at $t.centerLocation"
shiftTriangle(t)
println "Triangle is at $t.centerLocation"

println "Triangle is at $t.centerLocation"

t = ""

def x() {
    print "Hi"  // 5
}

def p = { g -> print g } // 13

t = [ x: 1, 
      y: 2, // 6
      z:4 ] // 7
t = [ 1, // 8
      2, // 9
      3] // 10
t = []; // 11
