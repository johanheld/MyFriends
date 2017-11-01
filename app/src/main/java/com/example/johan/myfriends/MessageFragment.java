package com.example.johan.myfriends;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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
    private Uri pictureUri;
    private String mCurrentPhotoPath;
    private byte[] photo;

    static final int THUMBNAIL = 1;
    static final int PICTURE = 2;

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
        btnText = (Button) view.findViewById(R.id.btnText);
        btnImage = (Button) view.findViewById(R.id.btnImage);
        listView = (ListView) view.findViewById(R.id.messageList);

        btnText.setOnClickListener(new NewTextListener());
        btnImage.setOnClickListener(new NewImageListener());

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
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Log.d("New group name", input.getText().toString());
                    controller.sendText(input.getText().toString());

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener()
            {

                @Override
                public void onShow(DialogInterface dialog)
                {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            dialog.show();
        }
    }

    private class NewImageListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            dispatchTakePictureIntent();
        }
    }

    //TODO Connection seem to be lost when camera is launched

    private void dispatchTakePictureIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(mainActivity.getPackageManager()) != null)
        {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "JPEG_" + timeStamp + ".jpg";
            File dir = mainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            pictureUri = Uri.fromFile(new File(dir, filename));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivityForResult(intent, PICTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == THUMBNAIL && resultCode == Activity.RESULT_OK)
        {
        } // thumbnail
        else if (requestCode == PICTURE && resultCode == Activity.RESULT_OK)
        {
            String pathToPicture = pictureUri.getPath();
            Log.d("PATH", pathToPicture);
            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mainActivity.getContentResolver(), pictureUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                photo = stream.toByteArray();
                controller.sendImage("HEJHEJ");

            } catch (IOException e)
            {
                e.printStackTrace();
            }
//            ivPicture.setImageBitmap(getScaled(pathToPicture, 500, 500));
        }
    }

    public byte[] getImage()
    {
        return photo;
    }
}