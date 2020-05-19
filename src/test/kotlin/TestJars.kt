
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestJars {

    @Test
    fun testMerge() {
        val original = getResource("testOriginalJar.jar")
        val patch = getResource("testPatchJar.jar")
        val dest = patch.parent.resolve("mergedJar.jar")
        val result = merge(original, patch, dest)
        assert(result.success())
        debugResultJar(dest)

        testJar(dest) {
            inClass("TestOriginalClass") {
                assertEquals("Replaced", "protectedStatic"())
                this["staticField"] = "amar"
                assertEquals("amar", this["staticField"])

                withInstance {
                    "privateVoid"<Int>()
                    assertEquals(23, this["notReplacingField"])
                    assertEquals(123, "notOverwritten"())
                    assertEquals(13, this["instanceField"])
                    assertEquals(69, "publicInt"())
                    assertEquals(12, "packageArgs"(listOf(Int::class.java, Int::class.java), 3, 4))
                }
            }

            inClass("TestInitializers") {
                assertEquals("ReplacedNoInit", this["staticFieldNoInitializerInitializer"])
                assertEquals("ReplaceInit", this["staticFieldInitializerInitializer"])
                assertEquals("OrigInitializerNoInitializer", this["staticFieldInitializerNoInitializer"])
                assertEquals(null, this["staticFieldNoInitializerNoInitializer"])

                withInstance {
                    assertEquals(1, this["instanceFieldNoInitializerInitializer"])
                    assertEquals(2, this["instanceFieldInitializerInitializer"])
                    assertEquals(33, this["instanceFieldInitializerNoInitializer"])
                    assertEquals(0, this["instanceFieldNoInitializerNoInitializer"])
                }
            }

            inClass("TestUnReplacedClass")

            inClass("TestAccess") {
                assert(field("privatePublicField").isPublic)
                assert(field("publicPrivateField").isPrivate)
                assert(method("privatePublicMethod").isPublic)
                assert(method("publicPrivateMethod").isPrivate)
            }

            inClass("TestInterface") {
                assertEquals("Replaced", "bar"())
            }

            inClass("TestInterfaceImpl") {
                withInstance {
                    assertEquals(10, "foo"())
                }
            }
        }
    }

    @Test
    fun testErrorJar() {
        val original = getResource("testOriginalJar.jar")
        val patch = getResource("testErrorPatchJar.jar")
        val dest = patch.parent.resolve("should_not_exist.jar")
        val result = merge(original, patch, dest)
        assert(result.errored())
        assertEquals(10, result.errors.size)
    }
}

