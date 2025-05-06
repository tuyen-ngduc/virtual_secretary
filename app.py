from flask import Flask, request, jsonify
import whisper
import os
import subprocess
import uuid
import tempfile
import glob

app = Flask(__name__)

UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Tải mô hình Whisper
model = whisper.load_model("base")

def convert_to_wav(input_path, output_path):
    command = [
        "ffmpeg",
        "-y",
        "-i", input_path,
        "-ar", "16000",
        "-ac", "1",
        "-c:a", "pcm_s16le",
        output_path
    ]
    subprocess.run(command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def split_audio(wav_path, segment_duration=60):
    segment_pattern = os.path.join(tempfile.gettempdir(), f"{uuid.uuid4().hex}_%03d.wav")
    command = [
        "ffmpeg",
        "-i", wav_path,
        "-f", "segment",
        "-segment_time", str(segment_duration),
        "-c", "copy",
        segment_pattern
    ]
    subprocess.run(command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return sorted(glob.glob(segment_pattern.replace('%03d', '*')))

@app.route('/transcribe', methods=['POST'])
def transcribe_audio():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    try:
        # Tạo tên file ngẫu nhiên
        unique_id = str(uuid.uuid4())
        original_ext = os.path.splitext(file.filename)[1].lower()
        input_path = os.path.join(UPLOAD_FOLDER, f"{unique_id}{original_ext}")
        wav_path = os.path.join(UPLOAD_FOLDER, f"{unique_id}.wav")

        file.save(input_path)
        convert_to_wav(input_path, wav_path)

        # Chia nhỏ file nếu dài quá
        segments = split_audio(wav_path, segment_duration=60)  # chia mỗi 60s
        full_transcript = ""

        for segment in segments:
            result = model.transcribe(
                segment,
                language="vi",
                temperature=0,
                condition_on_previous_text=False,
                verbose=False
            )
            full_transcript += result.get("text", "").strip() + " "

    except Exception as e:
        return jsonify({"error": f"Error during transcription: {str(e)}"}), 500

    finally:
        # Dọn file tạm
        for path in [input_path, wav_path] + segments:
            if os.path.exists(path):
                os.remove(path)

    return jsonify({"transcript": full_transcript.strip()})

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)