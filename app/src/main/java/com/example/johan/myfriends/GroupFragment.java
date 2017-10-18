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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment
{

    private ListView list;
    private Button btnUnsubscribe;
    private Button btnNewGroup;
    private Controller controller;
    private TextView etCurrentGroup;



    public GroupFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        init(view);
        return view;
    }

    public void setController(Controller controller)
    {
        this.controller = controller;
    }

    private void init(View view)
    {
        etCurrentGroup = (TextView)view.findViewById(R.id.tvCurrentGroup);
        btnUnsubscribe = (Button)view.findViewById(R.id.btnUnsubscribe);
        btnUnsubscribe.setOnClickListener(new UnsubscribeListener());
        btnNewGroup = (Button)view.findViewById(R.id.btnNewGroup);
        btnNewGroup.setOnClickListener(new NewGroupListener());
        list = (ListView) view.findViewById(R.id.list);

        if (MapsActivity.group != null)
            etCurrentGroup.setText(getString(R.string.current_group) + " " + MapsActivity.group);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String group = (String)parent.getItemAtPosition(position);
                controller.register(group);
            }
        });

    }

    public void setGroups(String [] groups)
    {
        String [] array = groups;
        list.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, array));
    }

    public void setCurrentGroup(String group)
    {
        etCurrentGroup.setText(getString(R.string.current_group) + " " + group);
    }

    private class NewGroupListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Title");

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
                    controller.register(input.getText().toString());
                    controller.getGroups();
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

    private class UnsubscribeListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            controller.unregister(MapsActivity.id);
            etCurrentGroup.setText("Current Group: ");
        }
    }

}
