package dev.jahir.frames.extensions.views

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.view.postDelayed
import coil.Coil
import coil.api.load
import coil.request.LoadRequestBuilder
import coil.transform.CircleCropTransformation
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

private const val CROSSFADE_DURATION = 200

private fun ImageView.buildSaturatingTarget(
    block: SaturatingImageViewTarget.() -> Unit
): SaturatingImageViewTarget = SaturatingImageViewTarget(this).apply(block)

private fun ImageView.buildRequestBuilder(
    thumbnail: Drawable?,
    cropAsCircle: Boolean,
    saturate: Boolean,
    extra: ((drawable: Drawable?) -> Unit)? = null
): LoadRequestBuilder.() -> Unit = {
    allowHardware(false)
    placeholder(thumbnail)
    error(thumbnail)

    if (thumbnail == null) crossfade(CROSSFADE_DURATION)
    else crossfade(false)

    if (cropAsCircle)
        transformations(CircleCropTransformation())

    val saturationTarget = buildSaturatingTarget {
        shouldActuallySaturate = saturate
        addListener { extra?.invoke(it) }
    }

    target(saturationTarget)
    listener(saturationTarget)
}

private fun ImageView.internalLoadFrames(
    url: String?,
    thumbnail: Drawable?,
    cropAsCircle: Boolean,
    saturate: Boolean,
    extra: ((drawable: Drawable?) -> Unit)? = null
) {
    Coil.load(
        context, url,
        builder = buildRequestBuilder(thumbnail, cropAsCircle, saturate, extra)
    )
}

fun ImageView.loadFramesPic(
    url: String,
    thumbnailUrl: String? = url,
    placeholderName: String? = "",
    forceLoadFullRes: Boolean = false,
    cropAsCircle: Boolean = false,
    saturate: Boolean = true,
    onImageLoaded: ((drawable: Drawable?) -> Unit)? = null
) {
    val shouldLoadThumbnail = thumbnailUrl?.let { it.hasContent() && it != url } ?: false
    val thumbnail = context.drawable(placeholderName)

    if (shouldLoadThumbnail) {
        if (context.preferences.shouldLoadFullResPictures || forceLoadFullRes) {
            internalLoadFrames(thumbnailUrl, thumbnail, cropAsCircle, saturate) {
                onImageLoaded?.invoke(it)
                internalLoadFrames(url, it, cropAsCircle, false, onImageLoaded)
            }
        } else {
            internalLoadFrames(
                thumbnailUrl, thumbnail, cropAsCircle, saturate, onImageLoaded
            )
        }
    } else {
        internalLoadFrames(url, thumbnail, cropAsCircle, saturate, onImageLoaded)
    }
}

fun ImageView.startAnimatable() {
    postDelayed(IMAGEVIEW_ANIMATABLE_DELAY) { (drawable as? Animatable)?.start() }
}

private const val IMAGEVIEW_ANIMATABLE_DELAY = 75L