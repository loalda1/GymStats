package org.gymstats.android
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class DemoNavigationTest {
    @get:Rule val activity=ActivityScenarioRule(MainActivity::class.java)
    @Test fun demoOpensDashboardThenRoutines(){onView(withId(R.id.demo)).perform(scrollTo(),click());onView(withId(R.id.demoBanner)).check(matches(isDisplayed()));onView(withId(R.id.routinesFragment)).perform(click());onView(withId(R.id.list)).check(matches(isDisplayed()))}
    @Test fun logoutReturnsToSignIn(){onView(withId(R.id.demo)).perform(scrollTo(),click());onView(withId(R.id.settingsFragment)).perform(click());onView(withId(R.id.logout)).perform(scrollTo(),click());onView(withId(R.id.submit)).check(matches(isDisplayed()))}
}
