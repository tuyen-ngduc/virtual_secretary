from flask import Flask, request, jsonify
import whisper
import os

app = Flask(__name__)
model = whisper.load_model("base")  # Hoặc bạn có thể dùng "tiny", "small" nếu cần tốc độ nhanh hơn

@app.route("/", methods=["POST"])
def transcribe_audio():
    if 'file' not in request.files:
        return jsonify({"error": "No file part in the request"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    # Lưu file tạm thời
    temp_path = "temp_audio.ogg"
    file.save(temp_path)

    try:
        # Chạy model Whisper để chuyển đổi âm thanh thành văn bản
        result = model.transcribe(temp_path)
        os.remove(temp_path)  # Xóa file tạm thời sau khi xử lý
        return jsonify({"text": result["text"]})
    except Exception as e:
        # Ghi lại lỗi chi tiết vào log
        app.logger.error(f"Error processing file {file.filename}: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(debug=True)  # Chạy Flask trên localhost:5000
