export async function uploadImageToCloudinary(file) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("upload_preset", "images");

  const res = await fetch(
    "https://api.cloudinary.com/v1_1/dklhqqcxf/image/upload",
    {
      method: "POST",
      body: formData
    }
  );

  const data = await res.json();
  return data.secure_url;
}
