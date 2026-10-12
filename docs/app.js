/**
 * SMS Query - Single Page Website Logic
 * - Animated Hero Phone Screen Recording Simulation
 * - Screenshot Carousel Navigation
 * - Interactive Query Sandbox
 * - Smooth Scroll & Social Interactions
 */

document.addEventListener("DOMContentLoaded", () => {
  initPhoneSimulation();
  initCarouselControls();
  initQuerySandbox();
  initSocialLinks();
});

/* -------------------------------------------------------------
 * 1. Animated Phone Screen Recording Simulation
 * Loops smoothly through:
 * 1. Inbox view
 * 2. Tap search button (ripple)
 * 3. Search form opens
 * 4. Simulated typing of "OTP 2-factor"
 * 5. SIM 2 toggle selected
 * 6. Tap Search button (ripple)
 * 7. Results displayed
 * 8. Return to Inbox
 * ----------------------------------------------------------- */
function initPhoneSimulation() {
  const stageInbox = document.getElementById("simStageInbox");
  const stageQuery = document.getElementById("simStageQuery");
  const stageResults = document.getElementById("simStageResults");
  const filterInputVal = document.getElementById("simFilterInputVal");
  const simPill1 = document.getElementById("simPill1");
  const simPill2 = document.getElementById("simPill2");
  const touchRipple = document.getElementById("simTouchRipple");
  const searchBtn = document.getElementById("simSearchBtn");
  const executeBtn = document.getElementById("simExecuteBtn");
  const timerText = document.getElementById("recTimer");
  const togglePlayBtn = document.getElementById("togglePlayBtn");

  let isPlaying = true;
  let timerSeconds = 0;
  let stepTimeout = null;

  // Recording timer increment
  setInterval(() => {
    if (!isPlaying) return;
    timerSeconds++;
    const mins = String(Math.floor(timerSeconds / 60)).padStart(2, "0");
    const secs = String(timerSeconds % 60).padStart(2, "0");
    if (timerText) timerText.textContent = `${mins}:${secs}`;
  }, 1000);

  function triggerRipple(x, y) {
    if (!touchRipple) return;
    touchRipple.style.left = `${x}px`;
    touchRipple.style.top = `${y}px`;
    touchRipple.classList.add("active");
    setTimeout(() => {
      touchRipple.classList.remove("active");
    }, 450);
  }

  function runSimulationLoop() {
    if (!isPlaying) return;

    // STEP 1: Inbox visible (0s)
    stageInbox.style.opacity = "1";
    stageInbox.style.transform = "translateY(0)";
    stageInbox.style.pointerEvents = "auto";
    
    stageQuery.style.opacity = "0";
    stageQuery.style.transform = "translateY(20px)";
    stageQuery.style.pointerEvents = "none";

    stageResults.style.opacity = "0";
    stageResults.style.transform = "translateY(20px)";
    stageResults.style.pointerEvents = "none";

    filterInputVal.textContent = "";
    simPill1.classList.add("active");
    simPill2.classList.remove("active");

    // STEP 2: At 2.0s, tap search button
    stepTimeout = setTimeout(() => {
      if (!isPlaying) return;
      triggerRipple(260, 48); // Near search icon
      
      // STEP 3: At 2.5s, slide in Query Filter form
      stepTimeout = setTimeout(() => {
        if (!isPlaying) return;
        stageInbox.style.opacity = "0";
        stageInbox.style.transform = "translateY(-10px)";
        
        stageQuery.style.opacity = "1";
        stageQuery.style.transform = "translateY(0)";
        stageQuery.style.pointerEvents = "auto";

        // STEP 4: At 3.2s, simulate typing "OTP 2-Factor"
        stepTimeout = setTimeout(() => {
          if (!isPlaying) return;
          const textToType = "OTP 2-Factor";
          let charIndex = 0;
          const typeInterval = setInterval(() => {
            if (!isPlaying) {
              clearInterval(typeInterval);
              return;
            }
            if (charIndex < textToType.length) {
              filterInputVal.textContent += textToType[charIndex];
              charIndex++;
            } else {
              clearInterval(typeInterval);

              // STEP 5: At 4.8s, tap SIM 2
              stepTimeout = setTimeout(() => {
                if (!isPlaying) return;
                triggerRipple(210, 160);
                simPill1.classList.remove("active");
                simPill2.classList.add("active");

                // STEP 6: At 5.8s, tap "Search"
                stepTimeout = setTimeout(() => {
                  if (!isPlaying) return;
                  triggerRipple(145, 345); // Search button

                  // STEP 7: At 6.2s, slide in Results
                  stepTimeout = setTimeout(() => {
                    if (!isPlaying) return;
                    stageQuery.style.opacity = "0";
                    stageQuery.style.transform = "translateY(-10px)";

                    stageResults.style.opacity = "1";
                    stageResults.style.transform = "translateY(0)";
                    stageResults.style.pointerEvents = "auto";

                    // STEP 8: At 10s, loop back to Inbox
                    stepTimeout = setTimeout(() => {
                      if (!isPlaying) return;
                      runSimulationLoop();
                    }, 4200);

                  }, 400);

                }, 1000);

              }, 800);
            }
          }, 90);

        }, 700);

      }, 450);

    }, 2000);
  }

  // Play / Pause toggle
  if (togglePlayBtn) {
    togglePlayBtn.addEventListener("click", () => {
      isPlaying = !isPlaying;
      togglePlayBtn.innerHTML = isPlaying
        ? `<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg> Pause`
        : `<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg> Play`;
      
      if (isPlaying) {
        runSimulationLoop();
      } else {
        clearTimeout(stepTimeout);
      }
    });
  }

  // Start simulation on load
  runSimulationLoop();
}

/* -------------------------------------------------------------
 * 2. Horizontal Scrollable Carousel Controls
 * ----------------------------------------------------------- */
function initCarouselControls() {
  const carousel = document.getElementById("screenshotCarousel");
  const prevBtn = document.getElementById("carouselPrev");
  const nextBtn = document.getElementById("carouselNext");

  if (!carousel || !prevBtn || !nextBtn) return;

  const cardWidth = 412; // 380px card + 32px gap

  prevBtn.addEventListener("click", () => {
    carousel.scrollBy({ left: -cardWidth, behavior: "smooth" });
  });

  nextBtn.addEventListener("click", () => {
    carousel.scrollBy({ left: cardWidth, behavior: "smooth" });
  });
}

/* -------------------------------------------------------------
 * 3. Interactive Query Sandbox Simulator
 * ----------------------------------------------------------- */
function initQuerySandbox() {
  const keywordInput = document.getElementById("sandboxKeyword");
  const simSelect = document.getElementById("sandboxSim");
  const senderSelect = document.getElementById("sandboxSender");
  const resultsContainer = document.getElementById("sandboxResults");
  const countLabel = document.getElementById("sandboxCount");

  if (!keywordInput || !simSelect || !senderSelect || !resultsContainer) return;

  const mockDatabase = [
    { id: 1, sender: "Amazon", sim: "SIM 1", time: "10:14 AM", text: "Your Amazon delivery OTP is 482910. Valid for 10 minutes." },
    { id: 2, sender: "Chase Bank", sim: "SIM 2", time: "09:30 AM", text: "Alert: A charge of $84.20 was approved at Whole Foods on card ending in 4102." },
    { id: 3, sender: "Google", sim: "SIM 1", time: "Yesterday", text: "G-931045 is your Google verification code. Never share this code." },
    { id: 4, sender: "Alice Brown", sim: "SIM 1", time: "Oct 10", text: "Hey! Let's sync on the project invoice and budget tomorrow morning." },
    { id: 5, sender: "Bob Johnson", sim: "SIM 2", time: "Oct 08", text: "Dinner confirmation for Friday evening. Looking forward to catching up!" },
    { id: 6, sender: "Delta Airlines", sim: "SIM 2", time: "Oct 05", text: "Flight DL 1842 to SFO is on time. Gate B24 boarding at 15:45." },
    { id: 7, sender: "Uber", sim: "SIM 1", time: "Oct 02", text: "Your Uber receipt: $24.50. Thank you for riding with us!" }
  ];

  function renderFilteredMessages() {
    const query = keywordInput.value.trim().toLowerCase();
    const selectedSim = simSelect.value;
    const selectedSender = senderSelect.value;

    const filtered = mockDatabase.filter(item => {
      const matchKeyword = query === "" || item.text.toLowerCase().includes(query) || item.sender.toLowerCase().includes(query);
      const matchSim = selectedSim === "ALL" || item.sim === selectedSim;
      const matchSender = selectedSender === "ALL" || item.sender === selectedSender;
      return matchKeyword && matchSim && matchSender;
    });

    countLabel.textContent = `Found ${filtered.length} matching message${filtered.length === 1 ? "" : "s"}`;

    if (filtered.length === 0) {
      resultsContainer.innerHTML = `
        <div style="text-align:center; padding: 24px; color: #64748b; font-size: 0.9rem;">
          No matching SMS found for this query filter. Try clearing keywords or changing SIM slot.
        </div>
      `;
      return;
    }

    resultsContainer.innerHTML = filtered.map(item => `
      <div class="result-row">
        <div class="result-row-left">
          <div class="result-sender-name">${escapeHtml(item.sender)} <span style="font-weight:400; color:#64748b; font-size:0.75rem;">• ${item.time}</span></div>
          <div class="result-snippet">${highlightKeyword(escapeHtml(item.text), query)}</div>
        </div>
        <div class="result-sim-tag">${item.sim}</div>
      </div>
    `).join("");
  }

  function highlightKeyword(text, keyword) {
    if (!keyword) return text;
    const regex = new RegExp(`(${keyword})`, "gi");
    return text.replace(regex, `<span class="sim-highlight">$1</span>`);
  }

  function escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  keywordInput.addEventListener("input", renderFilteredMessages);
  simSelect.addEventListener("change", renderFilteredMessages);
  senderSelect.addEventListener("change", renderFilteredMessages);

  // Initial render
  renderFilteredMessages();
}

/* -------------------------------------------------------------
 * 4. Fake Social Links & Tooltip Toast
 * ----------------------------------------------------------- */
function initSocialLinks() {
  const socialButtons = document.querySelectorAll(".social-icon-btn, .footer-social-row a");

  socialButtons.forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      const platform = btn.getAttribute("data-platform") || "Social Channel";
      showToast(`Redirecting to SMS Query official ${platform} page (Demo link).`);
    });
  });
}

function showToast(message) {
  let toast = document.getElementById("websiteToast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "websiteToast";
    toast.style.position = "fixed";
    toast.style.bottom = "24px";
    toast.style.right = "24px";
    toast.style.background = "#1e293b";
    toast.style.color = "#f8fafc";
    toast.style.padding = "12px 20px";
    toast.style.borderRadius = "10px";
    toast.style.fontSize = "0.85rem";
    toast.style.border = "1px solid rgba(255, 255, 255, 0.1)";
    toast.style.boxShadow = "0 8px 30px rgba(0, 0, 0, 0.4)";
    toast.style.zIndex = "9999";
    toast.style.transition = "all 0.3s ease";
    document.body.appendChild(toast);
  }

  toast.textContent = message;
  toast.style.opacity = "1";
  toast.style.transform = "translateY(0)";

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
  }, 2600);
}
