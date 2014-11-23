
class Test{
	def field1 = '111'
	def field2 = '222'
}

config {
/*
	// Before others - works ONLY without defined later options. Works if placed below (see down)
	digitsMix{
		one = 11
		two = 2
//		three = one + digits.two
//		four = one + digits.two + digitsMap.three
//		typed = new Test()
	}
*/

	// Before others! Works!
	digitsMixMap = [
		one: 11
		,two: 2
		,three: one + digits.two
		,four: one + digits.two + digitsMap.three
		,typed: new Test()
	]

	one = 1
	two = 2
	three = one + two

	digits {
		one = 1
		two = 2
		three = one + two
	}

	digitsMap = [
		one: 1
		,two: 2
		,three: one + two
	]

	digitsMix{
		one = 11
		two = 2
		three = one + digits.two
		four = one + digits.two + digitsMap.three
		typed = new Test()
	}
}
