package com.yhsrzbg.live_tv.build

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseConfigSmokeTest {
    @Test
    fun releaseProguardRules_coverRhinoJavaBeansReferences() {
        val configuredDir = System.getProperty("appTvModuleDir")?.let(::File)
        val cwd = File(System.getProperty("user.dir") ?: ".")
        val proguardRules = sequenceOf(
            configuredDir?.let { File(it, "proguard-rules.pro") },
            File(cwd, "proguard-rules.pro"),
            File(cwd, "app-tv/proguard-rules.pro"),
        ).filterNotNull().firstOrNull { it.exists() }
            ?: error("Unable to locate app-tv/proguard-rules.pro from ${cwd.absolutePath}")
        val content = proguardRules.readText()

        assertTrue(content.contains("-dontwarn java.beans.BeanDescriptor"))
        assertTrue(content.contains("-dontwarn java.beans.BeanInfo"))
        assertTrue(content.contains("-dontwarn java.beans.IntrospectionException"))
        assertTrue(content.contains("-dontwarn java.beans.Introspector"))
        assertTrue(content.contains("-dontwarn java.beans.PropertyDescriptor"))
    }
}
