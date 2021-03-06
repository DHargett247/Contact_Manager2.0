package com.example.epinesis_.contact_manager;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContentResolverCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Editable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int EDIT = 0, DELETE = 1;

    EditText Name, PhoneNum, Email, Address;
    ImageView contactImageImgView;
    List<Contact> Contacts = new ArrayList<Contact>();
    ListView contactListView;
    Uri imageUri = Uri.parse("android.resource://com.example.epinesis_.contact_manager/drawable/no_image.png");
    DatabaseHandler dbHandler;
    int longClickedItemIndex;
    ArrayAdapter<Contact> contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Name = (EditText)findViewById(R.id.name);
        PhoneNum = (EditText)findViewById(R.id.phoneNum);
        Email = (EditText)findViewById(R.id.Email);
        Address = (EditText)findViewById(R.id.Address);
        contactListView = (ListView)findViewById(R.id.listView);
        contactImageImgView =(ImageView)findViewById(R.id.imgViewContactImage);
        dbHandler = new DatabaseHandler(getApplicationContext());
        registerForContextMenu(contactListView);
        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                longClickedItemIndex = position;
                return false;
            }
        });

        TabHost tabhost = (TabHost)findViewById(R.id.tabHost);
        tabhost.setup();

        TabHost.TabSpec tabSpec = tabhost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Creator");
        tabhost.addTab(tabSpec);

        tabSpec = tabhost.newTabSpec("List");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("List");
        tabhost.addTab(tabSpec);


        final Button NewContact = (Button)findViewById(R.id.AddContact);
        NewContact.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {
                if(!Name.getText().toString().trim().isEmpty()){

                    Contact contact = new Contact(dbHandler.getContactCount(), String.valueOf(Name.getText()), String.valueOf(PhoneNum.getText().toString()),
                            String.valueOf(Email.getText()), String.valueOf(Address.getText().toString()), imageUri);
                    if (!contactExists(contact)){

                        dbHandler.createContact(contact);
                        Contacts.add(contact);
                        contactAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), Name.getText().toString() + "'s name has been added to your contacts!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),Name.getText() + "Already Exists", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "please enter a name!", Toast.LENGTH_SHORT).show();

            }

        });

        contactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), 1);
            }
        });
        if(dbHandler.getContactCount() != 0)
            Contacts.addAll(dbHandler.getAllContacts());

            populateList();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.setHeaderIcon(R.drawable.pencil_icon);
        menu.setHeaderTitle("Contact Options");
        menu.add(Menu.NONE, EDIT, menu.NONE, "Edit Contact");
        menu.add(Menu.NONE, DELETE, menu.NONE, "Delete Contact");
    }

    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case EDIT:
                break;
            case DELETE:
                dbHandler.deleteContact(Contacts.get(longClickedItemIndex));
                Contacts.remove(longClickedItemIndex);
                contactAdapter.notifyDataSetChanged();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private boolean contactExists(Contact contact){
        String name = contact.getName();
        int contactCount = Contacts.size();

        for (int i = 0; i < contactCount; i++)
        {
            if (name.compareToIgnoreCase(Contacts.get(i).getName())==0)
                return true;
        }
        return false;
    }
    public void onActivityResult(int reqCode, int resCode, Intent data)
    {
        if(resCode == RESULT_OK)
            if(reqCode == 1){
                imageUri = data.getData();
                contactImageImgView.setImageURI(data.getData());
        }
    }
    private void populateList()
    {
        contactAdapter = new ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
    }



    private class ContactListAdapter extends ArrayAdapter<Contact>
    {
        public ContactListAdapter()
        {
            super (MainActivity.this, R.layout.listview_item, Contacts);
        }

        public View getView(int position, View view, ViewGroup parent){
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView)view.findViewById(R.id.ContactName);
            name.setText(currentContact.getName());
            TextView phone = (TextView)view.findViewById(R.id.phone);
            phone.setText(currentContact.getPhone());
            TextView email = (TextView)view.findViewById(R.id.EmailAddress);
            email.setText(currentContact.getEmail());
            TextView address = (TextView)view.findViewById(R.id.Address);
            address.setText(currentContact.getAddress());
            ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
            ivContactImage.setImageURI(currentContact.get_imageURI());

            return view;
        }
    }

}
