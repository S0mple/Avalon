package avalon.function

fun main(args: Array<String>) {
	val status = CIs.get("AppVeyor")!!.getStatus("RayEldath/avalon")
	println(status)
}