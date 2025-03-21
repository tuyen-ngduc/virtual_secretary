import os
import whisper
from flask import Flask, request, jsonify, send_file

app = Flask(__name__)

UPLOAD_FOLDER = './uploads'
RESULT_FOLDER = './results'

os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(RESULT_FOLDER, exist_ok=True)

# Load Whisper model once
model = whisper.load_model("small")

@app.route('/transcribe', methods=['POST'])
def transcribe_audio():
    if 'audio' not in request.files:
        return jsonify({"error": "No audio file provided"}), 400

    audio_file = request.files['audio']
    filename = audio_file.filename

    # Save file temporarily
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    audio_file.save(file_path)

    # Transcribe using whisper
    result = model.transcribe(file_path, language="vi")
    text = result['text']

    # Save text result
    txt_filename = filename.rsplit('.', 1)[0] + ".txt"
    txt_path = os.path.join(RESULT_FOLDER, txt_filename)
    with open(txt_path, 'w', encoding='utf-8') as f:
        f.write(text)

    # Optional: remove file sau khi xử lý xong
    os.remove(file_path)

    # Trả về file text
    return send_file(txt_path, as_attachment=True)

if __name__ == '__main__':
    app.run(debug=True)
