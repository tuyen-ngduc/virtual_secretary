from flask import Flask, request, jsonify
import whisper
import os

app = Flask(__name__)

# Tạo thư mục lưu file tạm nếu chưa tồn tại
UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Tải model Whisper
model = whisper.load_model("base")

@app.route('/transcribe', methods=['POST'])
def transcribe_audio():
    # Kiểm tra nếu có tệp âm thanh trong request
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    # Lưu tệp âm thanh vào thư mục tạm an toàn trên Windows
    audio_path = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(audio_path)

    try:
        # Chạy Whisper để chuyển đổi âm thanh thành văn bản
        result = model.transcribe(audio_path)
        transcript_text = result.get("text", "")
    except Exception as e:
        return jsonify({"error": f"Error during transcription: {str(e)}"}), 500
    finally:
        # Xóa tệp âm thanh sau khi xử lý
        if os.path.exists(audio_path):
            os.remove(audio_path)

    # Trả về văn bản đã được chuyển đổi
    return jsonify({"transcript": transcript_text})


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
