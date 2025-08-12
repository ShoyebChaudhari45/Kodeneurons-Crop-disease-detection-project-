echo "# 🌿 Smart Crop Disease Detection

A hybrid mobile + Python project that detects crop diseases from leaf images using deep learning.

---

## 📱 Android App

- Allows farmers to **upload images of crop leaves**
- Receives prediction + treatment from Flask API
- Built using **Java + Retrofit**

---

## 🧠 Flask Backend

- CNN model trained with 39 plant disease classes
- Serves a prediction API at \`/predict\`
- Built using **PyTorch + Flask**
- Includes:
  - \`CNN.py\` – model architecture
  - \`plant_disease_model_1_latest.pt\` – trained model
  - \`app.py\` – Flask API with prediction logic
  - \`disease_info.csv\` – descriptions + treatments
  - \`supplement_info.csv\` – recommended supplements

---

## 🛠️ How to Run Backend

\`\`\`bash
cd backend
pip install -r requirements.txt
python app.py
\`\`\`

Runs at: \`http://127.0.0.1:5000\`

---

## 🚀 Future Scope

- Deploy backend (e.g. Render, PythonAnywhere)
- Add Firebase for user auth
- Include offline model inference

---

> Built with ❤️ by Shoyeb Chaudhari" > README.md
git add backend README.md
