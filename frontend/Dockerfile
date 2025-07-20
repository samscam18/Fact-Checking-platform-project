# Use the official Nginx image as a base
FROM nginx:alpine

# Copy your static frontend files into the Nginx default public directory
# The 'frontend' folder contains your index.html and associated JS/CSS.
# Ensure your index.html and other assets are directly in your frontend/ directory.
COPY . /usr/share/nginx/html

# Expose port 80, as Nginx runs on this port by default
EXPOSE 80

# Command to run Nginx (default command of the base image)
CMD ["nginx", "-g", "daemon off;"]

# Optional: Add metadata
LABEL maintainer="Samarth Firangi samarthfirangi44@gmail.com"
LABEL description="Docker image for Fact-Checking Platform Frontend (Nginx)"
