package com.example.chinyao.simpletodo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

// LISTENER
public class MainActivity extends AppCompatActivity implements EditItemFragment.EditItemListener {

    private final int DefaultItemCount = 3;

    private final int AddItemMode = 4;
    // 1: insert to the end via adapter plus scroll
    // 2: insert to the end via data source plus no scroll
    // 3: insert to the end via data source plus scroll
    // 4: insert to the beginning via data source plus scroll

    private final int BackEndMode = 2;
    // 1: file system
    // 2: Cupboard

    private final int CustomAdapter = 2;
    // 1: ArrayAdapter
    // 2: custom adapter

    private final int ActivityOrFragment = 2;
    // 1: Activity
    // 2: Fragment

    // need to use findViewById to get object from layout
    ListView lvItems;
    EditText etNewItem;

    // data
    ArrayList<TodoModel> items;

    // connect ArrayList and ListView
    ArrayAdapter<TodoModel> itemsAdapter;
    ItemAdapter itemsCustomAdapter;

    private final int REQUEST_CODE = 5566; // Intent

    SQLiteDatabase db;
    SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // need to use findViewById to get object from layout
        lvItems = (ListView) findViewById(R.id.lvItems);
        etNewItem = (EditText) findViewById(R.id.etNewItem);

        sdf = new SimpleDateFormat("MM/dd/yyyy");

        // create ArrayList
        if (BackEndMode == 2) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            db = dbHelper.getWritableDatabase();
        }
        readItems();

        //  connect ArrayList and ListView
        if (CustomAdapter == 1) {
            itemsAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, items);
            lvItems.setAdapter(itemsAdapter);
        }
        else if (CustomAdapter == 2) {
            itemsCustomAdapter = new ItemAdapter(this, items);
            lvItems.setAdapter(itemsCustomAdapter);
        }

        setListener();

        Toast.makeText(this, "Version 1606201950", Toast.LENGTH_SHORT).show();
        // update this with DatabaseHelper.java and app/build.gradle
    }

    public void setListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item,
                                                   int position,
                                                   long id) {
                        items.remove(position); // need to notify
                        notifyAdapter();

                        writeItems();
                        return true;
                    }
                }
        );
        lvItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter,
                                            View item,
                                            int position,
                                            long id) {
                        if (ActivityOrFragment == 1) {
                            Intent data = new Intent(MainActivity.this, EditItemActivity.class);
                            data.putExtra("position", position);
                            if (CustomAdapter == 1) {
                                data.putExtra("content", ((TextView) item).getText().toString());
                            } else if (CustomAdapter == 2) {
                                data.putExtra("content",
                                        ((TextView) ((RelativeLayout) item).getChildAt(0)).getText().toString());
                            }
                            startActivityForResult(data, REQUEST_CODE);
                        }
                        else if (ActivityOrFragment == 2) {
                            // LISTENER
                            FragmentManager fm = getSupportFragmentManager();
                            EditItemFragment theFragment =
                                    EditItemFragment.newInstance(position, items.get(position));
                            theFragment.show(fm, "fragment_edit_name");
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            String content = data.getStringExtra("content");

            if (position != -1) {
                items.get(position).content = content; // need to notify
                notifyAdapter();

                writeItems();
            }
        }
    }

    // LISTENER
    // This method is invoked in the activity when the listener is triggered
    // Access the data result passed to the activity here
    @Override
    public void onFinishEditItemListener(int position, TodoModel theTodoModel) {
        items.get(position).refresh(theTodoModel); // need to notify
        notifyAdapter();

        writeItems();
    }

    public void onAddItem(View view) {
        String itemText = etNewItem.getText().toString();
        TodoModel newItem = new TodoModel(itemText, "Low Priority", sdf.format(new Date()));
        if (!itemText.isEmpty()) {
            switch (AddItemMode) {
                case 1:
                    // 1: insert to the end via adapter plus scroll
                    if (CustomAdapter == 1) {
                        itemsAdapter.add(newItem); // cannot add to the beginning
                    }
                    else if (CustomAdapter == 2) {
                        itemsCustomAdapter.add(newItem); // cannot add to the beginning
                    }
                    lvItems.smoothScrollToPosition(lvItems.getCount() - 1);
                    break;
                case 2:
                    // 2: insert to the end via data source plus no scroll
                    items.add(newItem);
                    break;
                case 3:
                    // 3: insert to the end via data source plus scroll
                    items.add(newItem);
                    notifyAdapter();
                    lvItems.smoothScrollToPosition(lvItems.getCount() - 1); // need to notify
                    break;
                case 4:
                    // 4: insert to the beginning via data source plus scroll
                    items.add(0, newItem); // need to notify
                    notifyAdapter();
                    lvItems.smoothScrollToPosition(0); // need to notify
                    break;
                default:
                    break;
            }

            // hide keyboard
            // View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm =
                        (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            etNewItem.setText("");
            writeItems();
        }
    }

    private void notifyAdapter() {
        if (CustomAdapter == 1) {
            itemsAdapter.notifyDataSetChanged();
        }
        else if (CustomAdapter == 2) {
            itemsCustomAdapter.notifyDataSetChanged();
        }
    }

    private void defaultItems() {
        items = new ArrayList<>();
        for (int index = DefaultItemCount; index >= 1; index--) {
            items.add(new TodoModel("Item " + Integer.toString(index),
                    "Low Priority",
                    sdf.format(new Date())));
        }
    }

    private void readItems() {
        if (BackEndMode == 1) {
            File filesDir = getFilesDir();
            File todoFile = new File(filesDir, "todo.txt");
            try {
                ArrayList<String> inputs =
                        new ArrayList<String>(FileUtils.readLines(todoFile));
                for (int index = 0; index < inputs.size(); index++) {
                    items.add(new TodoModel(inputs.get(index),
                            "Low Priority",
                            sdf.format(new Date())));
                }
            } catch (IOException e) {
                e.printStackTrace();
                defaultItems();
                writeItems();
            }
        }
        else if (BackEndMode == 2) {
            // TODO: use Shared Preferences instead of SQL
            // http://guides.codepath.com/android/Persisting-Data-to-the-Device
            // https://developer.android.com/reference/android/content/SharedPreferences.html
            // get the first book in the result
            SettingModel theSettingModel = cupboard().withDatabase(db).query(SettingModel.class).get();
            if (theSettingModel == null) {
                defaultItems();
                writeItems();
                cupboard().withDatabase(db).put(new SettingModel(false));
            }
            else if (theSettingModel.firstTime == true) {
                defaultItems();
                writeItems();
                theSettingModel.firstTime = false;
                cupboard().withDatabase(db).put(theSettingModel);
            }

            // Get the cursor for this query
            Cursor cursorTodoModel = cupboard().withDatabase(db).query(TodoModel.class).getCursor();
            try {
                // Iterate Bunnys
                QueryResultIterable<TodoModel> itr =
                        cupboard().withCursor(cursorTodoModel).iterate(TodoModel.class);
                Iterator<TodoModel> it = itr.iterator();
                items = new ArrayList<>();
                while (it.hasNext()) {
                    TodoModel theTodoModel = it.next();
                    // do something with bunny
                    items.add(theTodoModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // close the cursor
                cursorTodoModel.close();
            }
        }
    }

    private void writeItems() {
        if (BackEndMode == 1) {
            File filesDir = getFilesDir();
            File todoFile = new File(filesDir, "todo.txt");
            try {
                FileUtils.writeLines(todoFile, items);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (BackEndMode == 2) {
            // TODO: only update the necessary items instead of all
            cupboard().withDatabase(db).delete(TodoModel.class, null);
            for (int index = 0; index < items.size(); index++) {
                cupboard().withDatabase(db).put(items.get(index));
            }
        }
    }
}
