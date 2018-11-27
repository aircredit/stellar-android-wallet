package com.blockeq.stellarwallet

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers

object AssetPage : BasePage() {
    override fun onPageLoaded(): AssetPage {
        onView(ViewMatchers.withId(R.id.assetsRecyclerView))
        return this
    }
}
