from flask import Flask, request, jsonify
from core.clip_utils import get_image_embedding, get_text_embedding
from core.faiss_manager import add_to_index, search_similar, reset_index
from core.storage import save_uploaded_image, remove_uploaded_image
from core import config

app = Flask(__name__)

@app.route("/add_image", methods=["POST"])
def add_image():
    if 'image' not in request.files:
        return jsonify({"error": "No image uploaded"}), 400
    uploaded_file = request.files['image']
    image_path = save_uploaded_image(uploaded_file)
    embedding = get_image_embedding(image_path)
    add_to_index(embedding, image_path)
    return jsonify({"message": "Image added successfully!"})

@app.route("/search_image", methods=["POST"])
def search_image():
    if 'image' not in request.files:
        return jsonify({"error": "No image uploaded"}), 400
    uploaded_file = request.files['image']
    image_path = save_uploaded_image(uploaded_file)
    embedding = get_image_embedding(image_path)
    remove_uploaded_image(image_path)
    similar_images = search_similar(embedding, config.TOP_K)
    return jsonify({"results": similar_images})

@app.route("/search_text", methods=["POST"])
def search_text():
    data = request.get_json()
    if not data or "query" not in data:
        return jsonify({"error": "No query provided"}), 400
    embedding = get_text_embedding(data["query"])
    similar_images = search_similar(embedding, config.TOP_K)
    return jsonify({"results": similar_images})

@app.route("/reset_index", methods=["POST"])
def reset():
    reset_index()
    return jsonify({"message": "Index reset successfully!"})

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000)
