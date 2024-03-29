package com.example.foodapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodapp.Model.Cart;
import com.example.foodapp.Prevalent.Prevalent;
import com.example.foodapp.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProccessBtn;
    private Button proceedBtn;
    private TextView txtTotalAmount;

    private int overTotalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProccessBtn = (Button) findViewById(R.id.next_process_btn);
        proceedBtn = (Button) findViewById(R.id.proceed_btn);
        txtTotalAmount = (TextView) findViewById(R.id.total_amount);

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                txtTotalAmount.setText("Total Price = LKR " + String.valueOf(overTotalPrice));

               Intent intent = new Intent(CartActivity.this, CartActivity.class);
               intent.putExtra("Total Price", String.valueOf(overTotalPrice));
               startActivity(intent);
               finish();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> options =
                new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View")
                        .child(Prevalent.currentOnlineUser.getPhone()),Cart.class)
                        .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
                = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull Cart model)
            {
                holder.txtProductQuantity.setText("Quantity = " + model.getQuantity());
                holder.txtProductPrice.setText("Price = LKR " + model.getPrice());
                holder.txtProductName.setText(model.getName());

                int oneTypeProductPrice = ((Integer.valueOf(model.getPrice()))) * Integer.valueOf(model.getQuantity());
                overTotalPrice = overTotalPrice + oneTypeProductPrice;


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Edit",
                                        "Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    //intent.putExtra("ItemKey", model.getItemKey());
                                    intent.putExtra("ItemKey",getRef(position).getKey());
                                    startActivity(intent);
                                }
                                if(i==1){
                                    cartListRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getPhone())
                                            .child(model.getItemKey())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(CartActivity.this, "Item Removed successfully", Toast.LENGTH_SHORT).show();

                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show() ;
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_layout, parent,false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
}