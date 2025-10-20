Image Similarity App is a desktop application that allows users to find visually similar images using CLIP embeddings and FAISS similarity search. It combines a Python backend for image processing and embedding search with a JavaFX frontend for a native desktop GUI experience.

Key Features

Search by Image: Upload an image to find the top visually similar images from the indexed dataset.

Search by Text: Input a textual description and retrieve images that match the text semantically.

Add Images to Index: Upload new images to expand the searchable dataset.

Reset Index: Clear the current image index and start fresh.

Fast Similarity Search: Uses FAISS for efficient nearest neighbor search on high-dimensional embeddings.

Tech Stack

Backend: Python, PyTorch, OpenAI CLIP, FAISS, Flask

Frontend: Java 21, JavaFX

Communication: JSON over HTTP REST API

How It Works

Images are converted into 512-dimensional embeddings using the CLIP model.

Embeddings are stored in a FAISS index for efficient similarity search.

The JavaFX desktop app sends queries to the Python Flask backend.

Backend returns a list of top similar images, which the frontend displays to the user.


