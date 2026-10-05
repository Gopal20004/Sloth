import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./auth";
import App from "./App";
import { MotionProvider } from "./animation";
import "./styles.css";
import "./showcase.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <MotionProvider><App /></MotionProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);
