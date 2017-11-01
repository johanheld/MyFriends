package com.example.johan.myfriends;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.johan.myfriends.Modules.TextMessage;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment
{
    private Button btnText;
    private Button btnImage;
    private ListView listView;
    private Controller controller;
    private ListAdapter adapter;
    private MainActivity mainActivity;

    public MessageFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        init(view);
        mainActivity = (MainActivity) getActivity();
        return view;
    }

    public void updateMessages(List<TextMessage> messages)
    {
        adapter = new ListAdapter(mainActivity, messages);
        listView.setAdapter(adapter);
    }

    private void init(View view)
    {
        btnText = (Button)view.findViewById(R.id.btnText);
        btnImage = (Button)view.findViewById(R.id.btnImage);
        listView = (ListView)view.findViewById(R.id.messageList);

        btnText.setOnClickListener(new NewTextListener());

        List messages = controller.getMessages();

        if (messages != null)
        {
            updateMessages(messages);
        }
    }

    public void setController(Controller controller)
    {
        this.controller = controller;
    }

    private class NewTextListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Message");

            // Set up the input
            final EditText input = new EditText(getContext());

            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d( "New group name" ,input.getText().toString());
                    controller.sendText(input.getText().toString());

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            dialog.show();
        }
    }
}
