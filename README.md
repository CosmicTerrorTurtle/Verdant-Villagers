# Verdant Villagers
This mod adds custom villagers that will gather resources and plan/build a village on their own. You can help them by providing resources that the villagers cannot find, and in return you will receive a portion of their farmed goods!

![5 village with roads houses small paths storage and farmland](https://user-images.githubusercontent.com/111278470/184848109-cd051815-868c-4753-8716-f123af53578e.png)

Currently work in progress; the mod is still in early development. Here's a quick overview of what features are implemented and what is planned for the future:

Already implemented:
- The village heart entity is the brain of the village. It plans, where roads and structures will be built (and currently places them immediately - in the future, this will be done by villagers).
- The village heart entity's spawn egg plus crafting recipe.
- The village anchor block is used for moving the village center. When in the world, it will call the nearest village heart entity (within 100 blocks), which will move towards the anchor and then hover over it.
- Block palettes for stone and wood. When spawned, the village scans its environment and decides which block palettes to use. For example, if there's a lot of oak wood around, the village's wood block palette will be oak. This means, that each structure that is built with wood blocks, will have oak logs, planks etc.

Coming in the future:
- Villagers with different professions (builder, farmer, miner, guard etc.) that gather resources, craft tools and blocks, build structures and roads etc.
- A villager trading system
- New village types: Biome-specific like desert/tundra villages with different buildings than the standard village type; underground villages, underwater villages, sky villages
- New block palette types: Colored blocks (wool, beds, glass blocks/panes, flowers, concrete, terracotta, banners, candles)
