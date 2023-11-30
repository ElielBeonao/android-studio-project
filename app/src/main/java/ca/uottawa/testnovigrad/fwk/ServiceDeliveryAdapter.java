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
import ca.uottawa.testnovigrad.models.ServiceDelivery;

public class ServiceDeliveryAdapter extends RecyclerView.Adapter<ServiceDeliveryAdapter.ServiceDeliveryViewHolder> {
    private static List<ServiceDelivery> serviceDeliveryList = new ArrayList<>();

    private static OnEntityModelButtonClickListener<ServiceDelivery> editButtonClickListener, deleteButtonClickListener;

    public ServiceDeliveryAdapter(List<ServiceDelivery> serviceDeliveryList) {
        this.serviceDeliveryList = serviceDeliveryList;
    }

    @Override
    public ServiceDeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_delivery, parent, false);
        return new ServiceDeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceDeliveryViewHolder holder, int position) {
        ServiceDelivery serviceDelivery = serviceDeliveryList.get(position);
        holder.bind(serviceDelivery);
    }

    @Override
    public int getItemCount() {
        return serviceDeliveryList.size();
    }

    public static class ServiceDeliveryViewHolder extends RecyclerView.ViewHolder {
        private TextView serviceDeliveryNameTextView, serviceDeliveryDescriptionTextView;
        private Button btnModifier, btnSupprimer;

        public ServiceDeliveryViewHolder(View itemView) {
            super(itemView);
            serviceDeliveryNameTextView = itemView.findViewById(R.id.ServiceDeliveryNameTextView);
            serviceDeliveryDescriptionTextView = itemView.findViewById(R.id.serviceDeliveryDescriptionTextView);

            btnModifier = itemView.findViewById(R.id.btn_service_delivery_modifier);
            btnSupprimer = itemView.findViewById(R.id.btn_service_delivery_supprimer);
        }

        public void bind(ServiceDelivery serviceDelivery) {

            if(!serviceDelivery.getName().isEmpty())
                serviceDeliveryNameTextView.setText(serviceDelivery.getName());

            if(!serviceDelivery.getDescription().isEmpty())
                serviceDeliveryDescriptionTextView.setText(serviceDelivery.getDescription());

            btnModifier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editButtonClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            editButtonClickListener.onEditButtonClick(serviceDeliveryList.get(position));
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
                            deleteButtonClickListener.onDeleteButtonClick(serviceDeliveryList.get(position));
                        }
                    }
                }
            });
        }
    }

    public void setOnEditButtonClickListener(OnEntityModelButtonClickListener<ServiceDelivery> listener) {
        this.editButtonClickListener = listener;
    }

    public void setOnDeleteButtonClickListener(OnEntityModelButtonClickListener<ServiceDelivery> listener) {
        this.deleteButtonClickListener = listener;
    }
}


