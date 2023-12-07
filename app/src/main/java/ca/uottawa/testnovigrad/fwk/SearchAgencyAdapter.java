package ca.uottawa.testnovigrad.fwk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.models.Agency;

public class SearchAgencyAdapter extends RecyclerView.Adapter<SearchAgencyAdapter.SearchAgencyViewHolder> {
    private static List<Agency> agencyList = new ArrayList<>();

    private static OnEntityModelButtonClickListener<Agency> editButtonClickListener, deleteButtonClickListener;

    public SearchAgencyAdapter(List<Agency> agencyList) {
        this.agencyList = agencyList;
    }

    @Override
    public SearchAgencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agency_search_result, parent, false);
        return new SearchAgencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchAgencyViewHolder holder, int position) {
        Agency agency = agencyList.get(position);
        holder.bind(agency);
    }

    @Override
    public int getItemCount() {
        return agencyList.size();
    }

    public static class SearchAgencyViewHolder extends RecyclerView.ViewHolder {
        private TextView agencyNameTextView, agencyAddressTextView, agencyOpenedAtTextView, agencyClosedAtTextView;
        private Button btnOpenAgencyDetails;

        private RatingBar ratingBarAgency;

        public SearchAgencyViewHolder(View itemView) {
            super(itemView);
            agencyNameTextView = itemView.findViewById(R.id.searchAgencyNameTextView);
            agencyAddressTextView = itemView.findViewById(R.id.searchAgencyAddressTextView);
            agencyOpenedAtTextView = itemView.findViewById(R.id.searchAgencyOpenedAtTextView);
            agencyClosedAtTextView = itemView.findViewById(R.id.searchAgencyClosedAtTextView);

            btnOpenAgencyDetails = itemView.findViewById(R.id.btn_agency_view_details);
            ratingBarAgency = itemView.findViewById(R.id.btn_agency_rating);
        }

        public void bind(Agency agency) {
            agencyNameTextView.setText(agency.getName());
            agencyAddressTextView.setText(agency.getAddress());
            if( agency.getOpenedAt() != null)
                agencyOpenedAtTextView.setText(ApplicationUtils.convertToDateTimeString(agency.getOpenedAt().toDate()));

            if( agency.getClosedAt() != null)
                agencyClosedAtTextView.setText(ApplicationUtils.convertToDateTimeString(agency.getClosedAt().toDate()));

//            btnModifier.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (editButtonClickListener != null) {
//                        int position = getAdapterPosition();
//                        if (position != RecyclerView.NO_POSITION) {
//                            editButtonClickListener.onEditButtonClick(agencyList.get(position));
//                        }
//                    }
//                }
//            });

//            btnSupprimer.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (deleteButtonClickListener != null) {
//                        int position = getAdapterPosition();
//                        if (position != RecyclerView.NO_POSITION) {
//                            deleteButtonClickListener.onDeleteButtonClick(agencyList.get(position));
//                        }
//                    }
//                }
//            });
        }
    }

    public void setOnEditButtonClickListener(OnEntityModelButtonClickListener<Agency> listener) {
        this.editButtonClickListener = listener;
    }

    public void setOnDeleteButtonClickListener(OnEntityModelButtonClickListener<Agency> listener) {
        this.deleteButtonClickListener = listener;
    }

    public void setAgencyList(List<Agency> agencies){
        agencyList = agencies;
    }
}