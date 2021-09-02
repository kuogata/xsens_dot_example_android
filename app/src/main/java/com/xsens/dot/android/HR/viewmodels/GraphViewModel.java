package com.xsens.dot.android.HR.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * A view model class for notifying data to views.
 */
public class GraphViewModel extends ViewModel {

    private static final String TAG = GraphViewModel.class.getSimpleName();

    /**
     * Get the instance of GraphViewModel
     *
     * @param owner The life cycle owner from activity/fragment
     * @return The GraphViewModel
     */
    public static GraphViewModel getInstance(@NonNull ViewModelStoreOwner owner) {

        return new ViewModelProvider(owner, new ViewModelProvider.NewInstanceFactory()).get(GraphViewModel.class);
    }

    // A variable to notify the streaming status
    private MutableLiveData<Boolean> mIsStreaming = new MutableLiveData<>();

    /**
     * Observe this function to listen the streaming status.
     *
     * @return The latest streaming status
     */

    public MutableLiveData<Boolean> isStreaming() {

        if (mIsStreaming.getValue() == null) mIsStreaming.setValue(false);
        return mIsStreaming;
    }
}