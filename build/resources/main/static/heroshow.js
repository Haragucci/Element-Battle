document.addEventListener('DOMContentLoaded', function() {
    const overlay = document.getElementById('overlay');
    let heroesData = [];

    fetch('/heroshow')
        .then(response => response.json())
        .then(data => {
            heroesData = data;
            displayHeroes(heroesData);
        })
        .catch(error => console.error('Error:', error));

    const searchBar = document.getElementById('search-bar');
    searchBar.addEventListener('input', function() {
        const query = searchBar.value.toLowerCase();

        const filteredHeroes = heroesData.filter(hero => {
            return hero.name.toLowerCase().includes(query) ||
                hero.id.toString().includes(query) ||
                hero.extra.toLowerCase().includes(query) ||
                hero.HP.toString().includes(query) ||
                hero.Damage.toString().includes(query) ||
                hero.type.toLowerCase().includes(query);
        });

        displayHeroes(filteredHeroes);
    });

    const deleteAllButton = document.getElementById('delete-all-button');
    deleteAllButton.addEventListener('click', () => {
        fetch('/delall', {
            method: 'DELETE'
        })
            .then(() => {
                const button = document.getElementById('delete-all-button');
                const heroList = document.getElementById('heroes');
                heroList.classList.add('deleting');
                heroList.innerHTML = '';
                overlay.classList.remove('active');
                setTimeout(() => {
                    button.classList.add('blink-success');
                }, 100);
                button.classList.remove('blink-success');
            })
            .catch(error => console.error('Error:', error));
    });

    function displayHeroes(heroes) {
        const heroList = document.getElementById('heroes');
        heroList.innerHTML = '';

        heroes.forEach(hero => {
            const li = document.createElement('li');
            li.innerHTML = `
            <div>
                <label>ID:</label>
                <input type="text" value="${hero.id}" disabled>
            </div>
            <div>
                <label>Name:</label>
                <input type="text" name="name" value="${hero.name}" disabled>
            </div>
            <div>
                <label>HP:</label>
                <input type="number" name="HP" value="${hero.HP}" min="0" max="15" disabled>
            </div>
            <div>
                <label>Schaden:</label>
                <input type="number" name="Damage" value="${hero.Damage}" min="0" max="15" disabled>
            </div>
            <div>
                <label>Element:</label>
                <select class="heroshow-select" name="type" disabled>
                    <option value="Feuer" ${hero.type === "Feuer" ? "selected" : ""}>Feuer</option>
                    <option value="Wasser" ${hero.type === "Wasser" ? "selected" : ""}>Wasser</option>
                    <option value="Pflanze" ${hero.type === "Pflanze" ? "selected" : ""}>Pflanze</option>
                    <option value="Elektro" ${hero.type === "Elektro" ? "selected" : ""}>Elektro</option>
                    <option value="Erde" ${hero.type === "Erde" ? "selected" : ""}>Erde</option>
                    <option value="Erde" ${hero.type === "Eis" ? "selected" : ""}>Eis</option>
                    <option value="Erde" ${hero.type === "Luft" ? "selected" : ""}>Luft</option>
                </select>
            </div>
            <div>
                <label>Extra:</label>
                <input type="text" name="extra" value="${hero.extra}" disabled>
            </div>
            <button class="edit-button">Bearbeiten</button>
            <button class="save-button">Speichern</button>
            <button class="cancel-button">Abbrechen</button>
            <button class="delete-button">Löschen</button>
        `;

            const editButton = li.querySelector('.edit-button');
            const saveButton = li.querySelector('.save-button');
            const cancelButton = li.querySelector('.cancel-button');
            const deleteButton = li.querySelector('.delete-button');
            const inputs = li.querySelectorAll('input');
            const select = li.querySelector('select');

            const applyButtonStyles = (button, backgroundColor, hoverColor) => {
                button.style.backgroundColor = backgroundColor;
                button.style.color = '#fff';
                button.style.border = 'none';
                button.style.padding = '10px 20px';
                button.style.borderRadius = '5px';
                button.style.cursor = 'pointer';
                button.style.marginBottom = '5px';
                button.addEventListener('mouseover', () => {
                    button.style.backgroundColor = hoverColor;
                });
                button.addEventListener('mouseout', () => {
                    button.style.backgroundColor = backgroundColor;
                });
            };

            applyButtonStyles(editButton, '#4CAF50', '#3e8e41');
            applyButtonStyles(saveButton, '#4CAF50', '#3e8e41');
            applyButtonStyles(cancelButton, '#FF5500', '#FF4500');
            applyButtonStyles(deleteButton, '#FF0000', '#CC0000');

            saveButton.style.display = 'none';
            cancelButton.style.display = 'none';

            let originalValues = {};

            editButton.addEventListener('click', () => {
                inputs.forEach((input, index) => {
                    if (index !== 0) {
                        input.disabled = false;
                        originalValues[input.name] = input.value;
                    }
                });
                select.style.width = 'calc(100% - 10px)';
                select.disabled = false;
                originalValues[select.name] = select.value;
                const overlay = document.getElementById('overlay');
                li.classList.add('editing');
                overlay.classList.add('active');
                editButton.style.display = 'none';
                saveButton.style.display = 'inline';
                cancelButton.style.display = 'inline';
            });

            saveButton.addEventListener('click', () => {
                const updatedHero = {
                    id: hero.id,
                    name: inputs[1].value,
                    HP: parseInt(inputs[2].value),
                    Damage: parseInt(inputs[3].value),
                    type: select.value,
                    extra: inputs[4].value
                };

                const ONLY_NUMBERS = /^\d+$/;

                let i = 0;

                if (updatedHero.name === "" ) {
                    setTimeout(() => {
                        inputs[1].classList.add('blink-error');
                    }, 100);
                    inputs[1].classList.remove('blink-error');
                    i++;
                }
                if (updatedHero.HP < 0 || updatedHero.HP > 15 || ONLY_NUMBERS.test(inputs[2].value) === false) {
                    setTimeout(() => {
                        inputs[2].classList.add('blink-error');
                    }, 100);
                    inputs[2].classList.remove('blink-error');
                    i++;
                }
                if (updatedHero.Damage < 0 || updatedHero.Damage > 15 || ONLY_NUMBERS.test(inputs[3].value) === false) {
                    setTimeout(() => {
                        inputs[3].classList.add('blink-error')
                    }, 100);
                    inputs[3].classList.remove('blink-error');
                    i++;
                }
                if (updatedHero.type !== "Feuer" && updatedHero.type !== "Wasser" && updatedHero.type !== "Pflanze" && updatedHero.type !== "Elektro" && updatedHero.type !== "Erde" && updatedHero.type !== "Eis" && updatedHero.type !== "Luft") {
                    setTimeout(() => {
                        select.classList.add('blink-error')
                    }, 100);
                    select.classList.remove('blink-error');
                    i++;
                }
                if (i >= 1) {
                    return;
                }

                fetch('/heroedit', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(updatedHero)
                })
                    .then(response => response.text())
                    .then(data => {
                        console.log('Success:', data);
                        const overlay = document.getElementById('overlay');
                        inputs.forEach(input => input.disabled = true);
                        select.disabled = true;
                        li.classList.remove('editing');
                        li.classList.add('saving');
                        li.classList.add('glowingtwo');
                        setTimeout(() => {
                            li.classList.remove('saving');
                            li.classList.remove('glowingtwo');
                        }, 1000);
                        overlay.classList.remove('active');
                        editButton.style.display = 'inline';
                        saveButton.style.display = 'none';
                        cancelButton.style.display = 'none';

                        const heroIndex = heroesData.findIndex(h => h.id === updatedHero.id);
                        if (heroIndex !== -1) {
                            heroesData[heroIndex] = updatedHero;
                        }
                    })
                    .catch(error => console.error('Error:', error));
            });

            cancelButton.addEventListener('click', () => {
                inputs.forEach((input, index) => {
                    if (index !== 0) {
                        input.value = originalValues[input.name];
                        input.disabled = true;
                    }
                });
                select.value = originalValues[select.name];
                select.disabled = true;
                const overlay = document.getElementById('overlay');
                li.classList.remove('editing');
                li.classList.add('cancelling');
                li.classList.add('glowingtwo');
                setTimeout(() => {
                    li.classList.remove('cancelling');
                    li.classList.remove('glowingtwo');
                }, 1000);
                overlay.classList.remove('active');
                editButton.style.display = 'inline';
                saveButton.style.display = 'none';
                cancelButton.style.display = 'none';
            });

            deleteButton.addEventListener('click', () => {
                li.classList.add('deleting');
                const heroListItems = Array.from(heroList.children);
                heroListItems.forEach(heroLi => {
                    heroLi.classList.add('glowing');
                });
                setTimeout(() => {
                    fetch(`/herodelete?id=${hero.id}`, {
                        method: 'DELETE'
                    })
                        .then(() => {
                            li.remove();
                            heroListItems.forEach(heroLi => {
                                heroLi.classList.remove('glowing');
                                heroLi.classList.add('moving-up');
                                heroLi.addEventListener('animationend', () => {
                                    heroLi.classList.remove('moving-up');
                                }, { once: true });
                            });
                            const overlay = document.getElementById('overlay');
                            if (overlay.classList.contains('active')) {
                                overlay.classList.remove('active');
                            }

                            heroesData = heroesData.filter(h => h.id !== hero.id);
                        })
                        .catch(error => console.error('Error:', error));
                }, 1000);
            });

            heroList.appendChild(li);
        });
    }
});
