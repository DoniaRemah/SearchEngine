import "./App.css";
import Search from "./Pages/Search/Search";
import { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Results from "./Pages/Results/Results";

function App() {
  return (
    <Router>
      <Routes>
        <Route exact path="/" element={<Search />} />
        <Route path="/results/:id" element={<Results />} />
      </Routes>
    </Router>
  );
}

export default App;
