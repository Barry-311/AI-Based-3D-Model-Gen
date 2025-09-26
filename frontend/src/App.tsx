import { Toaster } from "./components/ui/sonner";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import HomePage from "./pages/HomePage";
import ModelLibraryPage from "./pages/ModelLibraryPage";

function App() {
  return (
    <BrowserRouter>
      <div className="flex flex-col h-[100vh]">
        <header className="flex h-5 pl-5 pr-5 mt-5 mb-2 gap-x-10">
          <Header />
        </header>
        <main className="flex flex-col md:flex-row flex-1 p-5 gap-5">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/model-library" element={<ModelLibraryPage />} />
          </Routes>
        </main>
        <Toaster position="top-center" />
      </div>
    </BrowserRouter>
  );
}

export default App;
