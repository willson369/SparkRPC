const projectLink = document.getElementById("open-project");
if (projectLink) {
  projectLink.addEventListener("mouseenter", () => {
    projectLink.style.letterSpacing = "0.02em";
  });
  projectLink.addEventListener("mouseleave", () => {
    projectLink.style.letterSpacing = "";
  });
}

const nodes = document.querySelectorAll(".node");
nodes.forEach((node, index) => {
  node.style.opacity = "0";
  node.style.transform = "translateY(8px)";
  setTimeout(() => {
    node.style.transition = "opacity 420ms ease, transform 420ms ease";
    node.style.opacity = "1";
    node.style.transform = "none";
  }, 120 + index * 90);
});
