package com.matts.EpicMap;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.MyViewHolder> {

    private ArrayList<String> ids;
    private ArrayList<String> transport;
    private ArrayList<String> startAddress;
    private ArrayList<String> endAddress;

    Context context;

    // Variable used for the CardViews in the RecyclerView
    CardView cv;

    // SQL Lite Database Helper variable
    DBHelper db;

    public RouteAdapter(Context context, ArrayList<String> ids, ArrayList<String> transport, ArrayList<String> startAddress, ArrayList<String> endAddress) {

        db = new DBHelper(context);

        this.context = context;
        this.ids = ids;
        this.transport = transport;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_route_adapter, parent, false);
        cv = view.findViewById(R.id.cardView);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tvTransportMethod.setText("Transport Method: " + transport.get(position).substring(0, 1).toUpperCase()
                + transport.get(position).substring(1));
        holder.tvStartAddress.setText("Start: " + startAddress.get(position));
        holder.tvEndAddress.setText("End: " + endAddress.get(position));
        holder.tvDateTime.setText("Date/Time: " + ids.get(position));

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        // Variables to hold elements in the xml file
        TextView tvDateTime;
        TextView tvTransportMethod;
        TextView tvStartAddress;
        TextView tvEndAddress;

        // Constructor
        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            // Sets the text views for each element
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvTransportMethod = itemView.findViewById(R.id.tvTransportMethod);
            tvStartAddress = itemView.findViewById(R.id.tvFrom);
            tvEndAddress = itemView.findViewById(R.id.tvTo);

        }

    }

    @Override
    public int getItemCount() {
        return ids.size();
    }
}