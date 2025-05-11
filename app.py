from flask import Flask, request, jsonify
import os
from pydub import AudioSegment

app = Flask(__name__)

@app.route('/merge-audio', methods=['POST'])
def merge_audio():
    try:
        # Nhận dữ liệu từ request JSON
        data = request.get_json()

        # Kiểm tra xem meeting_code có tồn tại trong dữ liệu không
        if 'meeting_code' not in data:
            return jsonify({'status': 'error', 'message': 'Missing meeting_code in request'}), 400

        meeting_code = data['meeting_code']

        # Đường dẫn tới thư mục chứa các file âm thanh
        input_folder = f'audio/{meeting_code}'

        # Kiểm tra xem thư mục đầu vào có tồn tại không
        if not os.path.exists(input_folder):
            return jsonify({'status': 'error', 'message': f'Input folder for meeting {meeting_code} not found'}), 404

        # Thư mục để lưu file âm thanh đã nối
        output_folder = f'stt/{meeting_code}'
        os.makedirs(output_folder, exist_ok=True)

        # Khởi tạo đối tượng AudioSegment trống để nối các file âm thanh lại với nhau
        combined = AudioSegment.empty()

        # Lấy danh sách các file trong thư mục và sắp xếp theo tên
        files = sorted(os.listdir(input_folder))

        # Lặp qua các file trong thư mục và nối các file .ogg lại
        for file in files:
            if file.endswith('.ogg'):
                audio_path = os.path.join(input_folder, file)
                audio = AudioSegment.from_ogg(audio_path)
                combined += audio

        # Đặt tên file đầu ra theo định dạng "audio_cuoc_hop_{meeting_code}.ogg"
        output_path = os.path.join(output_folder, f'audio_cuoc_hop_{meeting_code}.ogg')

        # Xuất file âm thanh đã nối
        combined.export(output_path, format='ogg')

        # Trả về thông tin thành công
        return jsonify({'status': 'success', 'output': output_path}), 200

    except Exception as e:
        # Xử lý ngoại lệ nếu có lỗi
        return jsonify({'status': 'error', 'message': str(e)}), 500


if __name__ == '__main__':
    # Chạy Flask trên cổng 5010
    app.run(host='0.0.0.0', port=5010)
