package com.alvinhkh.buseta.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvinhkh.buseta.R;
import com.alvinhkh.buseta.holder.RouteBound;

public class RouteBoundAdapter extends StateSavingArrayAdapter<RouteBound> {

    // View lookup cache
    private static class ViewHolder {
        TextView origin;
        TextView destination;

        private ViewHolder(View convertView) {
            origin = (TextView) convertView.findViewById(R.id.origin);
            destination = (TextView) convertView.findViewById(R.id.destination);
        }
    }

    public RouteBoundAdapter(Context context) {
        super(context, R.layout.row_routebound);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RouteBound object = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_routebound, parent, false);
            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.origin.setText(object.origin_tc);
        viewHolder.destination.setText(object.destination_tc);
        // Return the completed view to render on screen
        return convertView;
    }
}