package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        Map<String, String> errors = new HashMap<>();

        validateFieldAndGlobalLogic(item, errors);

        if (!errors.isEmpty()) {
            log.info("errors={}", errors);
            model.addAttribute("errors", errors);
            return "/validation/v1/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    private void validateFieldAndGlobalLogic(Item item, Map<String, String> errors) {
        // ?????? ??????
        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "???????????? ????????? ???????????? ????????????.");
        }

        Integer price = item.getPrice();

        if (price != null && (price < 1000 || price > 1000000)) {
            errors.put("price", "????????? 1000??? ?????? 1????????? ????????? ???????????????.");
        }

        Integer quantity = item.getQuantity();

        if (quantity != null && quantity > 9999) {
            errors.put("quantity", "????????? ?????? 9999????????? ???????????????.");
        }

        // ????????? ??????
        if (price != null && quantity != null && price * quantity < 10000) {
            errors.put("globalError", "?????? * ????????? ?????? 10,000??? ??????????????? ?????????.");
        }
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item, Model model) {

        Map<String, String> errors = new HashMap<>();

        validateFieldAndGlobalLogic(item, errors);

        if (!errors.isEmpty()) {
            log.info("errors={}", errors);
            model.addAttribute("errors", errors);
            return "/validation/v1/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }

}

