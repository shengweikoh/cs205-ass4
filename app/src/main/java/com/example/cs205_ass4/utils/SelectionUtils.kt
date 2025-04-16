package com.example.cs205_ass4.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.example.cs205_ass4.utils.SelectionUtilsConstants

/**
 * Utility class to manage the selection and highlighting of UI elements
 * for implementing selection-based interactions.
 */
object SelectionUtils {
    
    /**
     * Manages selection state for a group of selectable items and their valid targets
     */
    class SelectionManager<T>(
        private val onItemSelected: ((T, View) -> Unit)? = null,
        private val onItemDeselected: (() -> Unit)? = null,
        private val onTargetInteraction: ((T, View) -> Unit)? = null
    ) {
        private var selectedItem: T? = null
        private var selectedView: View? = null
        private val selectableItems = mutableMapOf<View, T>()
        private val interactionTargets = mutableSetOf<View>()
        
        /**
         * Register a selectable item with the manager
         * @param view The view that can be selected
         * @param data The data object associated with this view
         */
        fun registerSelectableItem(view: View, data: T) {
            selectableItems[view] = data
            
            // Set up click listener
            view.isClickable = true
            view.setOnClickListener {
                if (it == selectedView) {
                    // Deselect if clicking the same item
                    clearSelection()
                } else {
                    // Select the new item
                    selectItem(it, data)
                }
            }
        }
        
        /**
         * Register a view as a valid interaction target when an item is selected
         * @param view The target view
         */
        fun registerInteractionTarget(view: View) {
            interactionTargets.add(view)
            
            // Target click handler
            view.setOnClickListener {
                selectedItem?.let { item ->
                    onTargetInteraction?.invoke(item, it)
                    clearSelection()
                }
            }
            
            // Initially not clickable until something is selected
            view.isClickable = false
        }
        
        /**
         * Programmatically select an item
         * @param view The view to select
         * @param data The data associated with the view
         */
        fun selectItem(view: View, data: T) {
            // First clear any existing selection
            clearSelection()
            
            // Set the new selection
            selectedView = view
            selectedItem = data
            
            // Highlight selected item
            view.background = SelectionUtilsConstants.SELECTED_ITEM_COLOR
            view.isSelected = true
            
            // Enable and highlight interaction targets
            interactionTargets.forEach { targetView ->
                targetView.foreground = SelectionUtilsConstants.TARGET_HIGHLIGHT_COLOR
                targetView.isClickable = true
            }
            
            // Notify callback
            onItemSelected?.invoke(data, view)
        }
        
        /**
         * Clear the current selection
         */
        fun clearSelection() {
            // Clear the selected view highlight
            selectedView?.let { view ->
                view.background = null
                view.isSelected = false
            }
            
            // Clear interaction targets
            interactionTargets.forEach { targetView ->
                targetView.foreground = null
                targetView.isClickable = false
            }
            
            // Reset selection state
            selectedItem = null
            selectedView = null
            
            // Notify callback
            onItemDeselected?.invoke()
        }
        
        /**
         * Get the currently selected item data
         */
        fun getSelectedItem(): T? = selectedItem
        
        /**
         * Get the currently selected view
         */
        fun getSelectedView(): View? = selectedView
        
        /**
         * Remove a selectable item from the manager
         */
        fun unregisterSelectableItem(view: View) {
            if (view == selectedView) {
                clearSelection()
            }
            selectableItems.remove(view)
            view.setOnClickListener(null)
        }
        
        /**
         * Remove an interaction target from the manager
         */
        fun unregisterInteractionTarget(view: View) {
            interactionTargets.remove(view)
            view.setOnClickListener(null)
            view.foreground = null
        }
        
        /**
         * Clean up resources and reset state
         */
        fun cleanup() {
            // Clear any active selection
            clearSelection()
            
            // Remove click listeners from all items
            selectableItems.keys.forEach { view -> 
                view.setOnClickListener(null)
            }
            
            // Clear all registered items
            selectableItems.clear()
            interactionTargets.clear()
        }
    }
} 