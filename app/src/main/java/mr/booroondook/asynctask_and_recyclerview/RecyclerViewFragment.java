package mr.booroondook.asynctask_and_recyclerview;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

public class RecyclerViewFragment extends Fragment {
    private String[] lastNames;
    private final ArrayList<String> arrayList = new ArrayList<>();
    private RecyclerAdapter recyclerAdapter;
    private RecyclerTask recyclerTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        lastNames = getResources().getStringArray(R.array.last_names);
        recyclerAdapter = new RecyclerAdapter(arrayList, getLayoutInflater());
        recyclerTask = new RecyclerTask();
        recyclerTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_view_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getActivity()),
                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onDestroy() {
        if (recyclerTask != null) {
            recyclerTask.cancel(false);
        }
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    class RecyclerTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (String name : lastNames) {
                if (isCancelled()) {
                    break;
                }
                publishProgress(name);
                SystemClock.sleep(500);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... items) {
            if (!isCancelled()) {
                recyclerAdapter.addName(items[0]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getActivity(), R.string.end_task, Toast.LENGTH_SHORT).show();
            recyclerTask = null;
        }
    }

    private static class RecyclerAdapter extends RecyclerView.Adapter<Holder> {
        private final ArrayList<String> arrayList;
        private final LayoutInflater layoutInflater;

        private RecyclerAdapter(ArrayList<String> arrayList, LayoutInflater layoutInflater) {
            this.arrayList = arrayList;
            this.layoutInflater = layoutInflater;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.list_cell, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.bindData(arrayList.get(position));
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        private void addName(String name) {
            arrayList.add(name);
            notifyItemInserted(arrayList.size() - 1);
        }
    }

    private static class Holder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final TextView unitName;
        private final TextView unitStatus;
        private final ImageView iconStatus;
        private String lastName;

        Holder(@NonNull View itemView) {
            super(itemView);
            unitName = itemView.findViewById(R.id.unit_name);
            unitStatus = itemView.findViewById(R.id.unit_status);
            iconStatus = itemView.findViewById(R.id.icon_status);
            itemView.setOnClickListener(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        view.findViewById(R.id.linear_layout_cell).getBackground()
                                .setHotspot(motionEvent.getX(), motionEvent.getY());
                        return false;
                    }
                });
            }
        }

        void bindData(String lastName) {
            this.lastName = lastName;
            unitName.setText(lastName);

            if (lastName.length() % 2 == 0) {
                unitStatus.setText(R.string.adopted);
                iconStatus.setImageResource(R.drawable.ic_check_24dp);
            } else {
                unitStatus.setText(R.string.fired);
                iconStatus.setImageResource(R.drawable.ic_close_24dp);
            }
        }

        @Override
        public void onClick(View view) {
            String status;
            if (lastName.length() % 2 == 0) {
                status = view.getContext().getResources().getString(R.string.adopted);
            } else {
                status = view.getContext().getResources().getString(R.string.fired);
            }
            Toast.makeText(view.getContext(),
                    String.format("%s:\n%s", lastName, status),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
