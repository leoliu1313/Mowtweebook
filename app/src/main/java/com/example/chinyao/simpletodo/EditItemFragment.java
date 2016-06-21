package com.example.chinyao.simpletodo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by chinyao on 6/20/2016.
 */

// LISTENER
public class EditItemFragment extends DialogFragment implements TextView.OnEditorActionListener {

    private int position;
    private EditText editContent;
    private EditText editPriority;
    private EditText editDate;

    public EditItemFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static EditItemFragment newInstance(int position, TodoModel theTodoModel) {
        EditItemFragment theFragment = new EditItemFragment();
        Bundle args = new Bundle();
        theFragment.position = position;
        args.putString("content", theTodoModel.content);
        args.putString("priority", theTodoModel.priority);
        args.putString("date", theTodoModel.date);
        theFragment.setArguments(args);
        return theFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_item, container);
    }

    // LISTENER
    // Defines the listener interface with a method passing back data result.
    public interface EditItemListener {
        void onFinishEditItemListener(int position, TodoModel theTodoModel);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        getDialog().setTitle(getArguments().getString("title", "Edit"));
        // Get field from view
        editContent = (EditText) view.findViewById(R.id.editContent);
        editContent.setText(getArguments().getString("content", ""));
        editPriority = (EditText) view.findViewById(R.id.editPriority);
        editPriority.setText(getArguments().getString("priority", ""));
        editDate = (EditText) view.findViewById(R.id.editDate);
        editDate.setText(getArguments().getString("date", ""));
        // Show soft keyboard automatically and request focus to field
        editContent.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // LISTENER
        // Setup a callback when the "Done" button is pressed on keyboard
        editDate.setOnEditorActionListener(this);
    }

    // LISTENER
    // Fires whenever the textfield has an action performed
    // In this case, when the "Done" button is pressed
    // REQUIRES a 'soft keyboard' (virtual keyboard)
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // Return input text back to activity through the implemented listener
            EditItemListener listener = (EditItemListener) getActivity();
            listener.onFinishEditItemListener(position,
                    new TodoModel(
                    editContent.getText().toString(),
                    editPriority.getText().toString(),
                    editDate.getText().toString()));
            // Close the dialog and return back to the parent activity
            dismiss();
            return true;
        }
        return false;
    }

}