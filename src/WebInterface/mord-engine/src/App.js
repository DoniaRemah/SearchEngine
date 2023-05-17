import "./App.css";
import Search from "./Pages/Search/Search";
import { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Results from "./Pages/Results/Results";

function App() {
  const [searchValue, setSearchValue] = useState("");
  const [response, setResponse] = useState("");

  return (
    <Router>
      <Routes>
        <Route
          exact
          path="/"
          element={
            <Search setResponse={setResponse} setSearchValue={setSearchValue} />
          }
        />
        <Route path="/results" element={<Results />} />
      </Routes>
    </Router>
  );
}

export default App;
