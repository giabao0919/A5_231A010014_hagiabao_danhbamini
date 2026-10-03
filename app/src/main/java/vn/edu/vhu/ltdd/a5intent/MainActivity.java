package vn.edu.vhu.ltdd.a5intent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // TODO: thay 2201234567 bằng MSSV của bạn
    private static final String TAG = "A5_231A010014";

    /** Khóa dùng chung cho hai màn hình — luôn khai báo hằng số, không gõ chuỗi hai lần. */
    public static final String EXTRA_CONTACT = "extra_contact";
    public static final String EXTRA_NGUOI_GUI = "extra_nguoi_gui";

    private EditText edtHoTen, edtDienThoai, edtEmail;
    private TextView tvKetQuaTraVe;

    /** Danh sách lưu trữ các liên hệ và vị trí liên hệ đang được chọn sửa. */
    private final ArrayList<Contact> danhSachContact = new ArrayList<>();
    private int viTriDangSua = -1;

    /** Bộ nhận kết quả trả về từ DetailActivity (thay cho onActivityResult đã lỗi thời). */
    private final ActivityResultLauncher<Intent> chiTietLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Contact daSua = IntentCompat.getParcelableExtra(
                            result.getData(), EXTRA_CONTACT, Contact.class);
                    if (daSua != null) {
                        edtHoTen.setText(daSua.getHoTen());
                        edtDienThoai.setText(daSua.getDienThoai());
                        edtEmail.setText(daSua.getEmail());

                        // CẬP NHẬT NGƯỜI CŨ HOẶC THÊM NGƯỜI MỚI VÀO DANH SÁCH
                        if (viTriDangSua >= 0 && viTriDangSua < danhSachContact.size()) {
                            // Trường hợp 1: Đang sửa người cũ được chọn từ danh sách
                            Contact nguoiCu = danhSachContact.get(viTriDangSua);
                            nguoiCu.setHoTen(daSua.getHoTen());
                            nguoiCu.setDienThoai(daSua.getDienThoai());
                            nguoiCu.setEmail(daSua.getEmail());
                            Log.d(TAG, "Cập nhật thành công người ở vị trí " + viTriDangSua + " thành: " + daSua.getHoTen());
                            viTriDangSua = -1; // Reset vị trí sau khi cập nhật xong
                        } else {
                            // Trường hợp 2: Người mới nhập -> Thêm vào danh sách (nếu chưa trùng)
                            boolean daTonTai = false;
                            for (Contact c : danhSachContact) {
                                if (c.getHoTen().equalsIgnoreCase(daSua.getHoTen()) &&
                                    c.getDienThoai().equals(daSua.getDienThoai())) {
                                    daTonTai = true;
                                    break;
                                }
                            }
                            if (!daTonTai) {
                                danhSachContact.add(daSua);
                                Log.d(TAG, "Đã thêm người mới vào danh sách: " + daSua.getHoTen() + " (Tổng số: " + danhSachContact.size() + ")");
                            }
                        }

                        tvKetQuaTraVe.setText(getString(R.string.returned, daSua.getHoTen()));
                        Log.d(TAG, "Nhận kết quả trả về: " + daSua.getHoTen());
                    }
                } else {
                    tvKetQuaTraVe.setText(R.string.returned_cancel);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        edtHoTen = findViewById(R.id.edtHoTen);
        edtDienThoai = findViewById(R.id.edtDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        tvKetQuaTraVe = findViewById(R.id.tvKetQuaTraVe);

        Button btnChiTiet = findViewById(R.id.btnChiTiet);
        Button btnGoi = findViewById(R.id.btnGoi);
        Button btnWeb = findViewById(R.id.btnWeb);
        Button btnChiaSe = findViewById(R.id.btnChiaSe);

        btnChiTiet.setOnClickListener(v -> moManHinhChiTiet());
        btnChiTiet.setOnLongClickListener(v -> {
            hienThiDanhSachVaChon(); // Nhấn giữ nút "Xem chi tiết" để hiển thị danh sách chọn
            return true;
        });
        btnGoi.setOnClickListener(v -> goiDien());
        btnWeb.setOnClickListener(v -> moTrangWeb());
        btnChiaSe.setOnClickListener(v -> chiaSe());
    }

    // ============ INTENT TƯỜNG MINH (explicit) ============

    private void moManHinhChiTiet() {
        // [Bước 1]: Lấy và kiểm tra họ tên không được để trống
        String hoTen = edtHoTen.getText().toString().trim();
        if (hoTen.isEmpty()) {
            edtHoTen.setError(getString(R.string.err_empty));
            return;
        }

        viTriDangSua = -1; // Mở trực tiếp, không phải sửa từ danh sách

        // [Bước 2]: Đóng gói dữ liệu vào đối tượng Contact
        Contact contact = new Contact(hoTen,
                edtDienThoai.getText().toString().trim(),
                edtEmail.getText().toString().trim());

        // [Bước 3]: Tạo Intent chuyển sang DetailActivity
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(EXTRA_CONTACT, contact);          // Đóng gói đối tượng Parcelable
        intent.putExtra(EXTRA_NGUOI_GUI, TAG);            // Đính kèm thông tin người gửi

        // [Bước 4]: Tạo hiệu ứng chuyển màn hình trượt ngang tự tạo (slide_in_right, slide_out_left)
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.slide_in_right,  // Màn hình 2 trượt vào từ bên phải
                R.anim.slide_out_left   // Màn hình 1 trượt sang bên trái
        );

        // [Bước 5]: Khởi chạy DetailActivity kèm theo gói hiệu ứng animation
        chiTietLauncher.launch(intent, options);
    }

    // ============ TRUYỀN & CHỌN TỪ DANH SÁCH ĐỐI TƯỢNG (Parcelable ArrayList) ============

    /** Hiển thị Popup danh sách liên hệ để người dùng lựa chọn chuyển sang Màn hình 2 chỉnh sửa. */
    private void hienThiDanhSachVaChon() {
        // [Bước 1]: Đảm bảo danh sách master có dữ liệu mẫu ban đầu
        if (danhSachContact.isEmpty()) {
            danhSachContact.add(new Contact("Nguyễn Văn A", "0901234567", "a@vhu.edu.vn"));
            danhSachContact.add(new Contact("Trần Thị B", "0987654321", "b@vhu.edu.vn"));
            danhSachContact.add(new Contact("Lê Văn C", "0912345678", "c@vhu.edu.vn"));
        }

        // [Bước 2]: Chuẩn bị mảng tên hiển thị cho AlertDialog
        String[] tenDanhSach = new String[danhSachContact.size()];
        for (int i = 0; i < danhSachContact.size(); i++) {
            Contact c = danhSachContact.get(i);
            tenDanhSach[i] = (i + 1) + ". " + c.getHoTen() + " (" + c.getDienThoai() + ")";
        }

        // [Bước 3]: Hiển thị AlertDialog dạng danh sách lựa chọn
        new AlertDialog.Builder(this)
                .setTitle("Chọn liên hệ cần xem & thay đổi")
                .setItems(tenDanhSach, (dialog, index) -> {
                    viTriDangSua = index; // Ghi nhớ chính xác vị trí người dùng đã chọn
                    Contact selected = danhSachContact.get(index);

                    // Đổ dữ liệu liên hệ được chọn lên ô nhập Màn hình 1
                    edtHoTen.setText(selected.getHoTen());
                    edtDienThoai.setText(selected.getDienThoai());
                    edtEmail.setText(selected.getEmail());

                    // Đóng gói Intent chuyển sang Màn hình 2
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra(EXTRA_CONTACT, selected);
                    intent.putExtra(EXTRA_NGUOI_GUI, TAG + " (Đã chọn người #" + (index + 1) + ")");
                    intent.putParcelableArrayListExtra("EXTRA_DS_CONTACT", danhSachContact);

                    // Thêm hiệu ứng chuyển màn hình
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                            this,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                    );
                    chiTietLauncher.launch(intent, options);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // ============ INTENT NGẦM ĐỊNH (implicit) ============

    private void goiDien() {
        String sdt = edtDienThoai.getText().toString().trim();
        if (sdt.isEmpty()) {
            edtDienThoai.setError(getString(R.string.err_empty));
            return;
        }
        // ACTION_DIAL chỉ mở màn hình gọi với số đã điền sẵn → KHÔNG cần xin quyền
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sdt));
        moAnToan(intent);
    }

    private void moTrangWeb() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.school_url)));
        moAnToan(intent);
    }

    private void chiaSe() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                edtHoTen.getText().toString(), edtDienThoai.getText().toString()));
        // Bọc trong bộ chọn để người dùng tự chọn ứng dụng
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    /**
     * Từ Android 11, resolveActivity() thường trả về null do cơ chế giới hạn hiển thị gói
     * (package visibility). Cách an toàn nhất là bắt ngoại lệ ActivityNotFoundException.
     */
    private void moAnToan(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.err_no_app, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không có ứng dụng nào xử lý: " + intent.getAction(), e);
        }
    }
}