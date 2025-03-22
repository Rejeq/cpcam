package com.rejeq.cpcam.core.common

interface ChildComponent {
    /**
     * Checks if the application or component is ready to be shown to the user.
     *
     * This function must be **fast** and non-blocking as it may be called on
     * the UI thread.
     *
     * @return `true` if the component wants to show itself.
     *         `false` if the component is not yet initialized.
     */
    fun readyToShow(): Boolean = true
}
