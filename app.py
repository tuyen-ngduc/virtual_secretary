from flask import Flask, request, jsonify
import whisper
import os
import subprocess
import uuid

app = Flask(__name__)

UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

model = whisper.load_model("medium")

def convert_to_wav(input_path, output_path):
    # Dùng FFmpeg để chuyển định dạng .ogg → .wav (mono, 16kHz)
    command = [
        "ffmpeg",
        "-y",  # Ghi đè nếu file đã tồn tại
        "-i", input_path,
        "-ar", "16000",  # Sample rate 16kHz
        "-ac", "1",      # Mono
        "-c:a", "pcm_s16le",  # PCM 16-bit
        output_path
    ]
    subprocess.run(command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

@app.route('/transcribe', methods=['POST'])
def transcribe_audio():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    try:
        # Tạo tên file ngẫu nhiên tránh trùng
        unique_id = str(uuid.uuid4())
        original_ext = os.path.splitext(file.filename)[1].lower()
        input_path = os.path.join(UPLOAD_FOLDER, f"{unique_id}{original_ext}")
        wav_path = os.path.join(UPLOAD_FOLDER, f"{unique_id}.wav")

        # Lưu file tạm
        file.save(input_path)

        # Convert sang WAV
        convert_to_wav(input_path, wav_path)

        # Chạy Whisper trên file WAV
        result = model.transcribe(wav_path)
        transcript_text = result.get("text", "")

    except Exception as e:
        return jsonify({"error": f"Error during transcription: {str(e)}"}), 500

#     finally:
#         # Xóa file tạm
#         for path in [input_path, wav_path]:
#             if os.path.exists(path):
#                 os.remove(path)

    return jsonify({"transcript": transcript_text})

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)