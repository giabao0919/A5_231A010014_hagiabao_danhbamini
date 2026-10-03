package vn.edu.vhu.ltdd.a5intent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "A5_231A010014";

    private Contact contact;
    private EditText edtHoTenMoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        TextView tvThongTin = findViewById(R.id.tvThongTin);
        TextView tvNguoiGui = findViewById(R.id.tvNguoiGui);
        edtHoTenMoi = findViewById(R.id.edtHoTenMoi);
        Button btnLuu = findViewById(R.id.btnLuu);
        Button btnHuy = findViewById(R.id.btnHuy);

        // Lấy dữ liệu do màn hình trước gửi sang
        contact = IntentCompat.getParcelableExtra(getIntent(),
                MainActivity.EXTRA_CONTACT, Contact.class);
        String nguoiGui = getIntent().getStringExtra(MainActivity.EXTRA_NGUOI_GUI);

        // [Bước 1]: Khai báo biến chứa danh sách đối tượng sẽ nhận được
        ArrayList<Contact> danhSachNhanDuoc;

        // [Bước 2]: Kiểm tra phiên bản Android để đọc dữ liệu đúng chuẩn
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Đối với Android 13 (API 33) trở lên: Truyền thêm Class<T> để đảm bảo an toàn kiểu dữ liệu (Type-safe)
            danhSachNhanDuoc = getIntent().getParcelableArrayListExtra("EXTRA_DS_CONTACT", Contact.class);
        } else {
            // Đối với các phiên bản Android 12 trở xuống (API < 33)
            danhSachNhanDuoc = getIntent().getParcelableArrayListExtra("EXTRA_DS_CONTACT");
        }

        // [Bước 3]: Kiểm tra dữ liệu khác null trước khi sử dụng
        if (danhSachNhanDuoc != null && !danhSachNhanDuoc.isEmpty()) {
            if (contact == null) {
                contact = danhSachNhanDuoc.get(0);
            }
            if (nguoiGui == null) {
                nguoiGui = "Danh sách (" + danhSachNhanDuoc.size() + " liên hệ)";
            }
            // Duyệt qua danh sách để xử lý (ví dụ: hiển thị lên ListView / RecyclerView)
            for (Contact c : danhSachNhanDuoc) {
                Log.d("A5_LOG", "Họ tên: " + c.getHoTen());
            }
        }

        if (contact == null) {                       // luôn phòng trường hợp không nhận được dữ liệu
            tvThongTin.setText(R.string.no_data);
            Log.w(TAG, "Không nhận được Contact từ Intent");
            return;
        }

        tvThongTin.setText(getString(R.string.detail_format,
                contact.getHoTen(), contact.getDienThoai(), contact.getEmail()));
        tvNguoiGui.setText(getString(R.string.sent_by, nguoiGui));
        edtHoTenMoi.setText(contact.getHoTen());

        btnLuu.setOnClickListener(v -> luuVaQuayLai());
        btnHuy.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);              // báo cho màn hình trước biết là đã hủy
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Hiệu ứng trượt ngược lại khi đóng
        });
    }

    /** Trả dữ liệu đã sửa về màn hình gọi. */
    private void luuVaQuayLai() {
        String hoTenMoi = edtHoTenMoi.getText().toString().trim();
        if (hoTenMoi.isEmpty()) {
            edtHoTenMoi.setError(getString(R.string.err_empty));
            return;
        }
        contact.setHoTen(hoTenMoi);

        Intent ketQua = new Intent();
        ketQua.putExtra(MainActivity.EXTRA_CONTACT, contact);
        setResult(RESULT_OK, ketQua);
        Log.d(TAG, "Trả kết quả về: " + hoTenMoi);
        finish();                                    // đóng màn hình này, quay về màn hình trước
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Hiệu ứng trượt ngược lại khi đóng
    }
}