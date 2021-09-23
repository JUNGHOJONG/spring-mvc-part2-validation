package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    private void init(WebDataBinder dataBinder) {
        log.info("dataBinder={}", dataBinder);
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        log.info("item={}", item);
        validateFieldAndGlobalLogic2(item, bindingResult);

        if (bindingResult.hasErrors()) {
            log.info("bindingResult={}", bindingResult);
            return "/validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /**
     * ItemValidator 클래스로 검증을 담당하는 부분을 분리한다.
     */
    @PostMapping("/add")
    public String addItemV2(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            log.info("bindingResult={}", bindingResult);
            return "/validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    private void validateFieldAndGlobalLogic(Item item, BindingResult bindingResult) {
        // 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false,
                    new String[]{"required.item.itemName"}, null, "상품명은 공백이 허용되지 않습니다."));
        }

        Integer price = item.getPrice();

        if (price == null || (price < 1000 || price > 1000000)) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,
                    new String[]{"range.item.price"}, new Object[]{1000, 1000000}, "가격은 1000원 이상 1백만원 이하만 허용합니다."));
        }

        Integer quantity = item.getQuantity();

        if (quantity == null || quantity > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,
                    new String[]{"max.item.quantity"}, new Object[]{9999}, "수량은 최대 9999까지만 허용합니다."));
        }

        // 글로벌 검증
        if (price != null && quantity != null && price * quantity < 10000) {
            bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"},
                    new Object[]{10000, (price * quantity)}, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + (price * quantity)));
        }
    }

    /**
     * 오류 코드와 메시지 처리2(개선)
     */
    private void validateFieldAndGlobalLogic2(Item item, BindingResult bindingResult) {
        // 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required");
        }

        Integer price = item.getPrice();

        if (price == null || (price < 1000 || price > 1000000)) {
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        Integer quantity = item.getQuantity();

        if (quantity == null || quantity > 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 글로벌 검증
        if (price != null && quantity != null && price * quantity < 10000) {
            bindingResult.reject("totalPriceMin", new Object[]{10000, (price * quantity)}, null);
        }
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item, BindingResult bindingResult, Model model) {

        validateFieldAndGlobalLogic(item, bindingResult);

        if (bindingResult.hasErrors()) {
            log.info("bindingResult={}", bindingResult);
            return "/validation/v2/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

