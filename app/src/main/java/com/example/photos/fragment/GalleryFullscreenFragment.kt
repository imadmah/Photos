package com.example.photos.fragment

import ZoomOutPageTransformer
import android.content.Context

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.photos.R
import com.example.photos.adapter.Image
import com.jsibbold.zoomage.ZoomageView
import java.lang.Math.abs


class GalleryFullscreenFragment : DialogFragment() {

    private var imageList = ArrayList<Image>()
    private var selectedPosition: Int = 0

    lateinit var viewPager: ViewPager

    lateinit var galleryPagerAdapter: GalleryPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gallery_fullscreen, container, false)
        viewPager = view.findViewById(R.id.viewPager)

        galleryPagerAdapter = GalleryPagerAdapter()
        imageList = arguments?.getSerializable("images") as ArrayList<Image>
        selectedPosition = requireArguments().getInt("position")
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, ZoomOutPageTransformer())

        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            private var isScrollingDown = false
            private var isScrollingLeft = false
            private var isScrollingRight = false
            private var initialX = 0f
            private var initialY = 0f
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager.SCROLL_STATE_IDLE -> {
                        if (isScrollingDown) {
                            requireActivity().onBackPressed()
                        } else if (isScrollingLeft) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        } else if (isScrollingRight) {
                            // The user scrolled right in the full-screen image view
                            // Handle right scroll behavior (e.g., show next image)
                        }

                        // Reset scroll flags and initial positions
                        isScrollingDown = false
                        isScrollingLeft = false
                        isScrollingRight = false
                        initialX = 0f
                        initialY = 0f
                    }
                    ViewPager.SCROLL_STATE_DRAGGING -> {
                        // Capture the initial touch position when dragging starts
                        initialX = viewPager.x
                        initialY = viewPager.y
                    }
                } // Do nothing
            }
             fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_MOVE) {
                    val deltaX = event.x - initialX
                    val deltaY = event.y - initialY

                    isScrollingDown = deltaY > 0 && abs(deltaY) > abs(deltaX)
                    isScrollingLeft = deltaX < 0 && abs(deltaX) > abs(deltaY)
                    isScrollingRight = deltaX > 0 && abs(deltaX) > abs(deltaY)
                }
                return false
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Do nothing
            }

            override fun onPageSelected(position: Int) {
                // Set the current position
                selectedPosition = position
            }
        } )

        setCurrentItem(selectedPosition)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle( STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, true)
    }

    // viewpager page change listener
    internal var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
            }
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            }
            override fun onPageScrollStateChanged(arg0: Int) {
            }
        }


    // Gallery adapter
    inner class GalleryPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = layoutInflater.inflate(R.layout.image_fullscreen, container, false)
            val imageView  = view.findViewById<ZoomageView>(R.id.ivFullscreenImage) // SubsamplingScaleImageView is used to zoom in and out of the image
            val image = imageList[position]
            // load image

            val displayMetrics = DisplayMetrics()
            val windowmanager = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)

            Glide.with(requireContext())
                .load(image.imagePath)
                .skipMemoryCache(true)
                .into(imageView)



            container.addView(view,0)
            return view
        }

        override fun getCount(): Int {
            return imageList.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }



}