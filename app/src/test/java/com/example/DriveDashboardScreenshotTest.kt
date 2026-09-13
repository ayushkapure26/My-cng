package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.ui.components.DriveDashboard
import com.example.ui.theme.CNGTrackTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class DriveDashboardScreenshotTest {
    @get:Rule val compose = createComposeRule()
    @Test fun overviewLight() {
        var refills = 0
        var pumps = 0
        compose.setContent {
            CNGTrackTheme(darkTheme = false) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(18.dp)) {
                        DriveDashboard("Ayush", "Super Carry CNG", "MH 18 AB 1234", 26.8,
                            4250.0, 650.0, 45.0, 7, { refills++ }, { pumps++ }, {}, {})
                    }
                }
            }
        }
        compose.onNodeWithTag("drive_add_refill").performClick()
        compose.onNodeWithTag("drive_find_pump").performClick()
        assertEquals(1, refills)
        assertEquals(1, pumps)
        compose.onRoot().captureRoboImage("build/ui-preview/home-light.png")
    }
    @Test fun overviewDarkEmpty() {
        compose.setContent {
            CNGTrackTheme(darkTheme = true) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(18.dp)) {
                        DriveDashboard("driver", null, null, null, 0.0, 0.0, 0.0, 0,
                            {}, {}, {}, {})
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("build/ui-preview/home-dark-empty.png")
    }
}
