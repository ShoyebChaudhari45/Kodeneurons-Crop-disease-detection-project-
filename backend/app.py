import os
from flask import Flask, request, jsonify
from PIL import Image
import torchvision.transforms.functional as TF
import torch
import numpy as np
import pandas as pd
import CNN

# Load CSV data
disease_info = pd.read_csv('disease_info.csv', encoding='cp1252')

# Load Model
model = CNN.CNN(39)
model.load_state_dict(torch.load("plant_disease_model_1_latest.pt", map_location=torch.device('cpu')))
model.eval()

# Create uploads folder if not exists
if not os.path.exists('static/uploads'):
    os.makedirs('static/uploads')

# Prediction function
def prediction(image_path):
    image = Image.open(image_path).convert('RGB')
    image = image.resize((224, 224))
    input_data = TF.to_tensor(image)
    input_data = input_data.view((-1, 3, 224, 224))
    output = model(input_data)
    output = output.detach().numpy()
    index = np.argmax(output)
    return index

# Flask app
app = Flask(__name__)

@app.route('/predict-api', methods=['POST'])
def predict_api():
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400
    
    image = request.files['image']
    lang = request.form.get('lang', 'en')
    filename = image.filename
    file_path = os.path.join('static/uploads', filename)
    image.save(file_path)

    pred = prediction(file_path)
    
    # Fetch data
    disease_name = disease_info['disease_name'][pred]
    description = disease_info['description'][pred]
    steps = disease_info['Possible Steps'][pred]

    # Check for language columns
    if lang == 'hi' and 'description_hi' in disease_info.columns:
        description = disease_info['description_hi'][pred]
        steps = disease_info['Possible Steps_hi'][pred]
    elif lang == 'mr' and 'description_mr' in disease_info.columns:
        description = disease_info['description_mr'][pred]
        steps = disease_info['Possible Steps_mr'][pred]

    response = {
        'disease': disease_name,
        'description': description,
        'treatment': steps,
        'image_url': disease_info['image_url'][pred]
    }
    return jsonify(response)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)