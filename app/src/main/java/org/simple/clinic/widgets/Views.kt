package org.simple.clinic.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.ViewFlipper
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import timber.log.Timber

fun EditText.showKeyboard() {
  post {
    this.requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
  }
}

fun ViewGroup.hideKeyboard() {
  post {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
  }
}

fun EditText.setTextAndCursor(textToSet: CharSequence?) {
  setText(textToSet)

  // Cannot rely on textToSet. It's possible that the
  // EditText modifies the text using InputFilters.
  setSelection(text.length)
}

fun View.setPadding(@DimenRes paddingRes: Int) {
  val padding = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(padding, padding, padding, padding)
}

fun View.setPaddingBottom(@DimenRes paddingRes: Int) {
  val newPaddingBottom = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(paddingStart, paddingTop, paddingEnd, newPaddingBottom)
}

fun View.setPaddingTop(@DimenRes paddingRes: Int) {
  val newPaddingTop = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(paddingStart, newPaddingTop, paddingEnd, paddingBottom)
}

fun View.setHorizontalPadding(@DimenRes paddingRes: Int) {
  val padding = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(padding, paddingTop, padding, paddingBottom)
}

fun View.setTopMargin(@DimenRes topMarginRes: Int) {
  val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
  marginLayoutParams.topMargin = resources.getDimensionPixelSize(topMarginRes)
  layoutParams = marginLayoutParams
}

fun TextView.setCompoundDrawableStart(@DrawableRes drawableRes: Int) {
  val drawable = ContextCompat.getDrawable(context, drawableRes)
  setCompoundDrawablesRelativeWithIntrinsicBounds(
      drawable,
      compoundDrawablesRelative[1],
      compoundDrawablesRelative[2],
      compoundDrawablesRelative[3])
}

fun TextView.setCompoundDrawableStart(drawable: Drawable?) {
  setCompoundDrawablesRelativeWithIntrinsicBounds(
      drawable,
      compoundDrawablesRelative[1],
      compoundDrawablesRelative[2],
      compoundDrawablesRelative[3])
}

/**
 * Run a function when a View gets measured and laid out on the screen.
 */
fun View.executeOnNextMeasure(runnable: () -> Unit) {
  if (isInEditMode || isLaidOut) {
    runnable()
    return
  }

  viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
    override fun onPreDraw(): Boolean {
      if (isLaidOut) {
        viewTreeObserver.removeOnPreDrawListener(this)
        runnable()

      } else if (visibility == View.GONE) {
        Timber.w("View's visibility is set to Gone. It'll never be measured: %s", resourceName())
        viewTreeObserver.removeOnPreDrawListener(this)
      }
      return true
    }
  })
}

/**
 * Like [View.getTop], but works even when a View is not the immediate child of [superParent].
 */
fun View.topRelativeTo(superParent: ViewGroup): Int {
  var totalDistance = 0
  var nextView = this

  while (true) {
    totalDistance += nextView.top
    if (nextView.parent == superParent) {
      break
    }
    nextView = nextView.parent as View

    if ((nextView.parent as ViewGroup).id == android.R.id.content) {
      throw AssertionError("${resources.getResourceEntryName(superParent.id)} isn't the parent of ${resources.getResourceEntryName(id)}")
    }
  }

  return totalDistance
}

@Suppress("FoldInitializerAndIfToElvis")
fun ViewGroup.indexOfChildId(@IdRes childId: Int): Int {
  val child = findViewById<View>(childId)
  if (child == null) {
    throw AssertionError("${resources.getResourceEntryName(childId)} isn't a part of ${resources.getResourceEntryName(id)}")
  }
  return indexOfChild(child)
}

fun View.resourceName() = resourceNameForId(resources, id)

fun resourceNameForId(resources: Resources, @IdRes id: Int): String {
  var name = "<nameless>"
  try {
    name = resources.getResourceEntryName(id)
  } catch (e: Resources.NotFoundException) {
    // Nothing to see here
  }

  return name
}

fun View.locationRectOnScreen(): Rect {
  val location = IntArray(2)
  getLocationOnScreen(location)

  val left = location[0]
  val top = location[1]
  return Rect(left, top, left + width, top + height)
}

val View.marginLayoutParams: ViewGroup.MarginLayoutParams
  get() = layoutParams as ViewGroup.MarginLayoutParams

fun dpToPx(dp: Float): Float {
  val metrics = Resources.getSystem().displayMetrics
  return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun dpToPx(dp: Int) = dpToPx(dp.toFloat())

fun TextView.setTextAppearanceCompat(@StyleRes resourceId: Int) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    setTextAppearance(resourceId)
  } else {
    @Suppress("DEPRECATION")
    setTextAppearance(context, resourceId)
  }
}

fun ScrollView.scrollToChild(view: View, onScrollComplete: () -> Unit = {}) {
  post {
    val distanceToScrollFromTop = view.topRelativeTo(this)

    smoothScrollTo(0, distanceToScrollFromTop)

    postDelayed(onScrollComplete, 400)
  }
}

var ViewFlipper.displayedChildResId: Int
  @IdRes
  get() = getChildAt(displayedChild).id
  set(@IdRes id) {
    val child = findViewById<View>(id)
    val childIndex = indexOfChild(child)
    displayedChild = childIndex
  }

fun <T> EditText.textChanges(mapper: (String) -> T): Observable<T> {
  return RxTextView.textChanges(this)
      .map { it.toString() }
      .map(mapper)
}
