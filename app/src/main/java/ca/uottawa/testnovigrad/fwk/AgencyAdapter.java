package ca.uottawa.testnovigrad.fwk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.User;

public class AgencyAdapter extends RecyclerView.Adapter<AgencyAdapter.AgencyViewHolder> {
    private static List<Agency> agencyList = new ArrayList<>();

    private static OnEntityModelButtonClickListener<Agency> editButtonClickListener, deleteButtonClickListener;

    public AgencyAdapter(List<Agency> agencyList) {
        this.agencyList = agencyList;
    }

    @Override
    public AgencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agency, parent, false);
        return new AgencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AgencyViewHolder holder, int position) {
        Agency agency = agencyList.get(position);
        holder.bind(agency);
    }

    @Override
    public int getItemCount() {
        return agencyList.size();
    }

    public static class AgencyViewHolder extends RecyclerView.ViewHolder {
        private TextView agencyNameTextView, agencyAddressTextView, agencyOpenedAtTextView, agencyClosedAtTextView;
        private Button btnModifier, btnSupprimer;

        public AgencyViewHolder(View itemView) {
            super(itemView);
            agencyNameTextView = itemView.findViewById(R.id.agencyNameTextView);
            agencyAddressTextView = itemView.findViewById(R.id.agencyAddressTextView);
            agencyOpenedAtTextView = itemView.findViewById(R.id.agencyOpenedAtTextView);
            agencyClosedAtTextView = itemView.findViewById(R.id.agencyClosedAtTextView);

            btnModifier = itemView.findViewById(R.id.btn_agency_modifier);
            btnSupprimer = itemView.findViewById(R.id.btn_agency_supprimer);
        }

        public void bind(Agency agency) {
            agencyNameTextView.setText(agency.getName());
            agencyAddressTextView.setText(agency.getAddress());
            if( agency.getOpenedAt() != null)
                agencyOpenedAtTextView.setText(ApplicationUtils.convertToDateTimeString(agency.getOpenedAt().toDate()));

            if( agency.getClosedAt() != null)
                agencyClosedAtTextView.setText(ApplicationUtils.convertToDateTimeString(agency.getClosedAt().toDate()));

            btnModifier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editButtonClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            editButtonClickListener.onEditButtonClick(agencyList.get(position));
                        }
                    }
                }
            });

            btnSupprimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteButtonClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            deleteButtonClickListener.onDeleteButtonClick(agencyList.get(position));
                        }
                    }
                }
            });
        }
    }

    public void setOnEditButtonClickListener(OnEntityModelButtonClickListener<Agency> listener) {
        this.editButtonClickListener = listener;
    }

    public void setOnDeleteButtonClickListener(OnEntityModelButtonClickListener<Agency> listener) {
        this.deleteButtonClickListener = listener;
    }
}


