package com.gianlu.commonutils.dialogs;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gianlu.commonutils.ui.Toaster;

public abstract class FragmentWithDialog extends Fragment implements DialogUtils.ShowStuffInterface {

    @Override
    @CallSuper
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof ActivityWithDialog))
            throw new IllegalStateException("Parent activity isn't instance of ActivityWithDialog!");
    }

    @Override
    public final void dismissDialog() {
        DialogUtils.dismissDialog(getActivity());
    }

    public final boolean hasVisibleDialog() {
        return DialogUtils.hasVisibleDialog(getActivity());
    }

    @Override
    public final void showDialog(@NonNull Dialog dialog) {
        DialogUtils.showDialog(getActivity(), dialog);
    }

    @Override
    public final void showDialog(@NonNull AlertDialog.Builder dialog) {
        DialogUtils.showDialog(getActivity(), dialog);
    }

    @Override
    public final void showToast(@NonNull Toaster toaster) {
        if (getContext() == null) return;
        toaster.show(getContext());
    }

    @Override
    public final void showDialog(@NonNull DialogFragment dialog) {
        showDialog(dialog, null);
    }

    @Override
    public final void showDialog(@NonNull DialogFragment fragment, @Nullable String tag) {
        DialogUtils.showDialog(getActivity(), fragment, tag);
    }

    @Override
    public final void showProgress(@StringRes int res) {
        if (getContext() == null) return;
        showDialog(DialogUtils.progressDialog(getContext(), res));
    }

    public final void onBackPressed() {
        if (getActivity() != null) getActivity().onBackPressed();
    }
}
