// src/pages/main/Home.jsx
import React, { useEffect, useState } from "react";
import { MapPin } from "lucide-react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import { useNavigate } from "react-router-dom";
import { apiGet } from "../../api/api.js";
import Navbar from "../../components/Navbar";
import MenuModal from "../../components/MenuModal";

// Local banner images
import banner1 from "../../assets/clubs/1.jpg";
import banner2 from "../../assets/clubs/2.jpg";
import banner3 from "../../assets/clubs/3.jpg";
import banner4 from "../../assets/clubs/4.jpg";
import banner5 from "../../assets/clubs/5.jpg";
import banner6 from "../../assets/clubs/1.jpg";
import banner7 from "../../assets/clubs/2.jpg";
import banner8 from "../../assets/clubs/3.jpg";
import banner9 from "../../assets/clubs/4.jpg";
import banner10 from "../../assets/clubs/5.jpg";

const BANNERS = [banner1, banner2, banner3, banner4, banner5,banner6,banner7,banner8,banner9,banner10];


const markerIcon = new L.Icon({
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

export default function Home() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [clubs, setClubs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [activeMarkerId, setActiveMarkerId] = useState(null);
  const [activeIndex, setActiveIndex] = useState(0); // қай клуб баннерде тұр

  const navigate = useNavigate();
  const almatyCenter = [43.238949, 76.889709];
// Клубтарды жүктеу
useEffect(() => {
  let mounted = true;

  (async () => {
    setLoading(true);
    setLoadError(null);

    try {
      const res = await apiGet("/clubs");
      const data = Array.isArray(res) ? res : res?.data ?? res;

      if (mounted) {
        if (Array.isArray(data)) {

          // ➜ IMPORTED LOCAL IMAGES
          // (You must add these imports at the top of the file!)
          //
          // import banner1 from "../../assets/clubs/1.jpg";
          // import banner2 from "../../assets/clubs/2.jpg";
          // import banner3 from "../../assets/clubs/3.jpg";
          // import banner4 from "../../assets/clubs/4.jpg";
          // import banner5 from "../../assets/clubs/5.jpg";
          //
          const BANNERS = [banner1, banner2, banner3, banner4, banner5,banner6,banner7,banner8,banner9,banner10];

          // ➜ Attach a banner to each club
          const enhanced = data.map((club, index) => ({
            ...club,
            bannerUrl: BANNERS[index % BANNERS.length], // cycles 1..5
          }));

          setClubs(enhanced);
          setActiveIndex(0);

        } else {
          console.warn("Unexpected /clubs response shape:", data);
          setClubs([]);
        }
      }
    } catch (err) {
      console.error("Ошибка загрузки клубов:", err);
      if (mounted) {
        setLoadError(err.message || "Ошибка загрузки");
        setClubs([]);
      }
    } finally {
      if (mounted) setLoading(false);
    }
  })();

  return () => {
    mounted = false;
  };
}, []);


  const activeClub = clubs[activeIndex] || null;

  const openClub = (club) => navigate(`/clubs/${club.id}`);

  return (
    <div className="min-h-screen bg-[#05050b] text-white font-sans">
      <Navbar onMenuClick={() => setMenuOpen(true)} />
      <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 pt-8 pb-16">
        {/* Location & title */}
        <div className="flex flex-col items-center gap-4 mb-8">
          <div className="flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-4 py-2 shadow-sm backdrop-blur">
            <MapPin size={16} className="text-blue-400" />
            <span className="text-sm font-medium text-gray-100">Алматы</span>
          </div>

          <div className="text-center space-y-2">
            <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight">
              Видеоигровые клубы Алматы
            </h1>
            <p className="text-xs sm:text-sm text-gray-400">
              Просмотрите баннеры ваших любимых клубов или выберите один на карте.
            </p>
          </div>
        </div>

        {/* HERO + SIDE LIST */}
        <section className="grid gap-6 lg:grid-cols-[minmax(0,3fr)_minmax(0,1.1fr)] mb-10">
          {/* LEFT: негізгі баннер */}
          <div className="relative rounded-3xl overflow-hidden bg-gradient-to-r from-[#090812] via-[#090818] to-[#050509] border border-white/10 shadow-2xl">
            {/* Баннер суреті (егер бар болса) */}
            {activeClub?.bannerUrl && (
              <img
                src={activeClub.bannerUrl}
                alt={activeClub.name}
                className="absolute inset-0 w-full h-full object-cover"
              />
            )}

            {/* Қара градиент, чтобы текст хорошо оқылсын */}
            <div className="absolute inset-0 bg-gradient-to-r from-black/85 via-black/70 to-black/10" />

            {/* Контент */}
            <div className="relative z-10 flex flex-col justify-between h-full px-6 sm:px-10 py-6 sm:py-8">
              {/* Upper text */}
              <div className="space-y-4 max-w-lg">
                <div className="inline-flex items-center gap-2 text-[11px] uppercase tracking-[0.18em] text-gray-300">
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 shadow shadow-emerald-400/70" />
                  <span>Smart Club</span>
                </div>

                <h2 className="text-2xl sm:text-3xl font-semibold leading-tight">
                  {activeClub ? activeClub.name : "Клуб таңдау жүріп жатыр..."}
                </h2>

                <p className="text-xs sm:text-sm text-gray-300 leading-relaxed">
                  {activeClub?.description ||
                    "Алматыдағы заманауи киберспорт клубы. Жоғары FPS, ыңғайлы орындығылар және достық атмосфера – бәрі бір жерде."}
                </p>

                {activeClub?.address && (
                  <p className="text-[11px] sm:text-xs text-gray-400">
                    📍 {activeClub.address}
                  </p>
                )}
              </div>

              {/* Bottom actions */}
              <div className="mt-6 flex flex-wrap items-center gap-3">
                <button
                  onClick={() => activeClub && openClub(activeClub)}
                  className="inline-flex items-center justify-center rounded-full bg-white text-black px-5 py-2 text-sm font-medium shadow-md hover:bg-gray-100 transition"
                >
                  Подробнее
                </button>

                {activeClub?.priceFrom && (
                  <div className="ml-auto text-right text-xs sm:text-sm text-gray-300">
                    <div className="text-[10px] uppercase tracking-widest text-gray-500">
                      Цена за час
                    </div>
                    <div className="font-semibold">
                      {activeClub.priceFrom} ₸+
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* RIGHT: ойын тізімі / клубтар тізімі */}
          <aside className="space-y-3">
            <p className="text-xs sm:text-sm text-gray-400 uppercase tracking-[0.16em]">
              Все клубы
            </p>

            <div className="flex flex-col gap-2 max-h-[430px] overflow-y-auto pr-1 custom-scroll">
              {loading && (
                <div className="text-xs text-gray-500 py-4 px-2">
                  Клубы загружается...
                </div>
              )}

              {loadError && !loading && (
                <div className="text-xs text-red-400 py-4 px-2">
                  Ошибка: {loadError}
                </div>
              )}

              {!loading && !loadError && clubs.length === 0 && (
                <div className="text-xs text-gray-500 py-4 px-2">
                  Клубы не найдены
                </div>
              )}

              {clubs.map((club, idx) => {
                const isActive = idx === activeIndex;
                const thumb =
                  club.thumbnailUrl || club.bannerUrl || club.imageUrl;

                return (
                  <button
                    key={club.id ?? idx}
                    type="button"
                    onClick={() => setActiveIndex(idx)}
                    className={`group w-full flex items-center gap-3 rounded-2xl px-2 py-2 sm:px-3 sm:py-2.5 text-left transition ${
                      isActive
                        ? "bg-white/10 border border-white/15"
                        : "bg-transparent hover:bg-white/5 border border-transparent"
                    }`}
                  >
                    <div className="relative flex-shrink-0 w-10 h-10 sm:w-12 sm:h-12 rounded-xl overflow-hidden bg-[#222]">
                      {thumb ? (
                        <img
                          src={thumb}
                          alt={club.name}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-xs text-gray-400">
                          {club.name?.[0] || "?"}
                        </div>
                      )}
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="text-sm font-medium truncate">
                        {club.name}
                      </div>
                      {club.shortAddress && (
                        <div className="text-[11px] text-gray-400 truncate">
                          {club.shortAddress}
                        </div>
                      )}
                    </div>

                    {isActive && (
                      <div className="text-[10px] uppercase tracking-[0.16em] text-blue-400">
                        Active
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          </aside>
        </section>

        {/* MAP SECTION */}
        <section className="space-y-3">
          <p className="text-center text-xs sm:text-sm text-gray-400">
            Или выберите через карту:
          </p>

          <div className="relative rounded-2xl border border-blue-500/60 bg-gradient-to-br from-blue-500/10 via-purple-500/10 to-transparent overflow-hidden shadow-xl shadow-black/60">
            <div className="absolute top-3 left-4 z-10 rounded-full bg-black/60 backdrop-blur px-3 py-1 text-[11px] text-gray-200 border border-white/10">
              Карта компьютерных клубов
            </div>

            <MapContainer
              center={almatyCenter}
              zoom={12}
              scrollWheelZoom={true}
              style={{ height: "480px", width: "100%" }}
            >
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

              {clubs
                .filter((c) => c.latitude && c.longitude)
                .map((club) => (
                  <Marker
                    key={club.id}
                    position={[club.latitude, club.longitude]}
                    icon={markerIcon}
                    eventHandlers={{
                      click: (e) => {
                        if (activeMarkerId === club.id) {
                          openClub(club);
                        } else {
                          setActiveMarkerId(club.id);
                          try {
                            e.target.openPopup();
                          } catch {}
                        }
                      },
                      mouseover: (e) => {
                        setActiveMarkerId(club.id);
                        try {
                          e.target.openPopup();
                        } catch {}
                      },
                      mouseout: () => {},
                    }}
                  >
                    <Popup>
                      <div
                        className="max-w-xs space-y-1"
                        onClick={() => openClub(club)}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") openClub(club);
                        }}
                        style={{ cursor: "pointer" }}
                      >
                        <div className="font-semibold text-sm mb-1">
                          {club.name}
                        </div>
                        {club.address && (
                          <div className="text-[11px] text-gray-600 mb-2">
                            {club.address}
                          </div>
                        )}
                        <div className="flex gap-2">
                          <button
                            onClick={(ev) => {
                              ev.stopPropagation();
                              openClub(club);
                            }}
                            className="px-2 py-1 rounded bg-blue-600 text-white text-[11px] hover:bg-blue-700 transition"
                          >
                            Подробнее
                          </button>
                          {club.phone && (
                            <a
                              href={`tel:${club.phone.replace(/\D/g, "")}`}
                              onClick={(ev) => ev.stopPropagation()}
                              className="px-2 py-1 rounded border border-gray-300 text-[11px] text-gray-800 bg-white hover:bg-gray-100 transition"
                            >
                              Позвонить
                            </a>
                          )}
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                ))}
            </MapContainer>
          </div>
        </section>
      </main>
    </div>
  );
}
