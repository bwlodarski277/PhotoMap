package bwlodarski.photoMap.activities;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import bwlodarski.photoMap.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LoginActivityTest {
	@Rule
	public ActivityTestRule<LoginActivity> activityTestRule = new ActivityTestRule<>(LoginActivity.class);

	@Test
	public void testShortLogin() {
		onView(withId(R.id.email)).perform(typeText("abc"), closeSoftKeyboard());
		onView(withId(R.id.loginButton)).perform(click());
		onView(withId(R.id.resetButton)).check(matches(isDisplayed()));
	}

	@Test
	public void testInvalidLogin() {
		onView(withId(R.id.email)).perform(typeText("TestEmail"), closeSoftKeyboard());
		onView(withId(R.id.password)).perform(typeText("TestPassword"), closeSoftKeyboard());
		onView(withId(R.id.resetButton)).check(matches(isDisplayed()));
	}
}